package cz.ackee.gradle.type

class CustomBuildTypeFactory(
    private val versionCode: Int
) {

    fun createBuildTypes(): List<CustomBuildType> {
        return listOf(
            CustomBuildType.Debug,
            CustomBuildType.Beta(versionCode),
            CustomBuildType.Release
        )
    }
}
