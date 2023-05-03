package cc.unitmesh.importer.model

object PackageUtil {
    /** input examples:
     * 1. storage/src/main/kotlin/com/waz/zclient/storage/db/userclients/UserClientDao.kt
     * 2. storage/src/main/java/com/waz/zclient/storage/db/userclients/UserClientDao.kt
     * output:
     * 1. com.waz.zclient.storage.db.userclients.UserClientDao
     * 2. com.waz.zclient.storage.db.userclients.UserClientDao
     **/
    fun pathToIdentifier(path: String): String {
        val regex = Regex("(kotlin|java)/(.+)\\.kt")
        val match = regex.find(path)
        return match?.groupValues?.get(2)?.replace('/', '.') ?: ""
    }
}
