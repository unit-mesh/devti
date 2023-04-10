package cc.unitmesh.importer

import cc.unitmesh.importer.model.RawDump
import io.kotest.matchers.shouldBe
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
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

    @Test
    fun should_generate_prompts() {
        val snippets = snippetsFromFile(File("src/test/resources/snippets.json"))
        val typeStrings = File("src/test/resources/types.json").readText()
        val types: List<RawDump> = Json.decodeFromString(typeStrings)

        val prompts = snippetTypesToPrompts(
            types,
            snippets
        )

        assertEquals(4, prompts.size)
        prompts[0] shouldBe """请编写用户故事，能覆盖下面的代码功能，要求：1. 分析其业务含义，突出重点  2. 你返回的内容只有： 我想 xxx。

###
data class ZhuanlanBean( var followersCount: Int, var creator: Creator, var topics: List<Topic>, var activateState: String, var href: String, var acceptSubmission: Boolean, var firstTime: Boolean, var pendingName: String, var avatar: Avatar, var canManage: Boolean, var description: String, var nameCanEditUntil: Int, var reason: String, var banUntil: Int, @PrimaryKey var slug: String, var name: String, var url: String, var intro: String, var topicsCanEditUntil: Int, var activateAuthorRequested: String, var commentPermission: String, var following: Boolean, var postsCount: Int, var canPost: Boolean, var type: Int = 0 )

@Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(zhuanlanBean: ZhuanlanBean): Long
###"""
    }
}
