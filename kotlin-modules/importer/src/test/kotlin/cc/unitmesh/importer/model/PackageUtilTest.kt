package cc.unitmesh.importer.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class PackageUtilTest {
    @Test
    fun should_parse_package_from_path() {
        val path = "storage/src/main/kotlin/com/waz/zclient/storage/db/userclients/UserClientDao.kt"
        val identifier = PackageUtil.pathToIdentifier(path)
        assertEquals("com.waz.zclient.storage.db.userclients.UserClientDao", identifier)
    }
}