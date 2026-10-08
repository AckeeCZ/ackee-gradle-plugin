# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Published Gradle plugin that applies Ackee's standard setup to Android **application** modules: build types,
signing, `app.properties`, versionCode, lint, JaCoCo, git hooks, and artifact copying. Feature docs for
consumers live in `README.md`.

- Maven coordinates: `io.github.ackeecz:build-gradle-plugin`; root package `io.github.ackeecz.gradle`
- Version lives in `build-gradle-plugin/lib.properties` (`VERSION`), alongside the POM metadata
- Targets **AGP 9** (new DSL, built-in Kotlin). AGP is `compileOnly`, so the consumer's AGP version is the
  one used at runtime
- Single version catalog shared by all builds: `gradle/libs.versions.toml`

## Repository Structure

The root is only an aggregator of **included builds** (no root project code):

```
build-gradle-plugin/              — The plugin (kotlin-dsl). Plugin IDs registered in its build.gradle.kts.
build-gradle-plugin/build-logic/  — Included build: internal publishing convention plugin (vanniktech + Dokka, reads lib.properties).
sample/                           — Android app consuming the plugin via pluginManagement.includeBuild. Not published.
```

`sample` resolves the plugin from source, so plugin changes are exercised just by building the sample.

## Architecture

Five plugin IDs (`io.github.ackeecz.plugin.<suffix>` → `plugin/<ClassName>`):

| ID suffix | Class | Does |
|---|---|---|
| `build` | `AckeeGradlePlugin` | Applies the four below |
| `config` | `ConfigureAppPlugin` | `appProperties` extension + `extra`, sets `versionCode` |
| `variants` | `VariantsPlugin` | Debug/Beta/Release build types + signing configs from `keystore.properties` |
| `verifications` | `VerificationsPlugin` | Strict lint, JaCoCo tasks (on the **parent** project), copy `.githooks` |
| `deployment` | `DeploymentPlugin` | Copies APK/AAB/mapping to `<root>/outputs`, changelog check before `appDistributionUpload*` |

- Everything goes through `ApplicationAndroidComponentsExtension` (`getApplicationAndroidComponents()`), so
  the plugins only work on `com.android.application` modules. DSL changes go in `finalizeDsl { }`,
  per-variant task wiring in `onVariants { }` — no legacy `android { }` / `BaseExtension` APIs.
- versionCode = `CI_VERSION_CODE` env var, falling back to `git rev-list HEAD --count` (`VersionCodeProvider`).
- Build types / signing configs are modelled as data (`type/Custom*`), produced by `*Factory` and applied
  with `*Creator.maybeCreate`, so consumer-declared types with the same name are kept and extended.
- Copy tasks extend `task/copy/FileCopyTask`; each exposes a static `registerTask(...)`. Tasks are
  `@DisableCachingByDefault` and declare `@PathSensitive` on inputs.
- `CodeCoverage.kt` is legacy (marked TODO) and configured through `project.ext` values
  (`jacocoExcludedProjects`, `jacocoTestVariant`, `jacocoExcludedFiles`).

## Commands

Run from the repo root; included builds are addressed by their directory name.

```
./gradlew :build-gradle-plugin:assemble                      # build the plugin (same as CI)
./gradlew :sample:app:assembleBeta                           # exercise the plugin end-to-end
./gradlew :build-gradle-plugin:publishToMavenLocal           # test in another project via mavenLocal()
```

There are no unit tests in the plugin — verification is building `sample`.

## Releasing

1. Bump `VERSION` in `build-gradle-plugin/lib.properties`.
2. Move `[Unreleased]` in `CHANGELOG.MD` to the new version (Keep a Changelog format). Every user-facing
   change gets a CHANGELOG entry under `[Unreleased]`.
3. Pushing a git tag triggers `.github/workflows/publish.yml` → `publishAndReleaseToMavenCentral`. Signing
   secrets exist only in CI.
