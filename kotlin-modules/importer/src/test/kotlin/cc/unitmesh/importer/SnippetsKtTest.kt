package cc.unitmesh.importer

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File

class SnippetsKtTest {
    @Test
    fun test() {
        /**
         * package com.meiji.daily.data.local.dao
         *
         * import android.arch.persistence.room.Dao
         * import android.arch.persistence.room.Insert
         * import android.arch.persistence.room.OnConflictStrategy
         * import android.arch.persistence.room.Query
         *
         * import com.meiji.daily.bean.ZhuanlanBean
         *
         * import io.reactivex.Maybe
         *
         * @Dao
         * interface ZhuanlanDao {
         *
         *     @Insert(onConflict = OnConflictStrategy.IGNORE)
         *     fun insert(zhuanlanBean: ZhuanlanBean): Long
         *
         *     @Insert(onConflict = OnConflictStrategy.IGNORE)
         *     fun insert(list: MutableList<ZhuanlanBean>)
         *
         *     @Query("SELECT * FROM zhuanlans WHERE type = :type")
         *     fun query(type: Int): Maybe<MutableList<ZhuanlanBean>>
         *
         *     @Query("DELETE FROM zhuanlans WHERE slug = :slug")
         *     fun delete(slug: String)
         * }
         *
         */
        val snippets = snippetsFromFile(File("src/test/resources/snippets.json"))
        assertEquals(4, snippets.size)

        snippets[0].identifierName shouldBe "com.meiji.daily.data.local.dao.ZhuanlanDao"
        snippets[0].requiredType shouldBe listOf("com.meiji.daily.bean.ZhuanlanBean", "Long")
    }
}