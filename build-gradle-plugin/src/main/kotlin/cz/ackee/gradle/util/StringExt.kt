package cz.ackee.gradle.util

import java.util.Locale

fun String.capitalizeFirstChar(locale: Locale = Locale.getDefault()): String {
    return replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
}
