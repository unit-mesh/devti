package cc.unitmesh.importer.model

object PackageUtil {
    fun pathToIdentifier(path: String): String {
        val pathWithoutExtension = path.substringBeforeLast(".")
        val pathWithoutSrc = pathWithoutExtension.substringAfterLast("src/main/kotlin/")
        return pathWithoutSrc.replace("/", ".")
    }
}
