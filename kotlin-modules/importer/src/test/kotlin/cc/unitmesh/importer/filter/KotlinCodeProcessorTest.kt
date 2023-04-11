package cc.unitmesh.importer.processor

import cc.unitmesh.importer.model.RawDump
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class KotlinCodeProcessorTest {
    val rawData =
        """{"repo_name":"KcgPrj/HouseHoldAccountBook","path":"src/main/kotlin/jp/ac/kcg/repository/ItemRepository.kt","copies":"1","size":"584","content":"package jp.ac.kcg.repository\n\nimport jp.ac.kcg.domain.Item\nimport jp.ac.kcg.domain.User\nimport org.springframework.data.jpa.repository.JpaRepository\nimport org.springframework.data.jpa.repository.Query\nimport org.springframework.data.repository.query.Param\nimport java.time.LocalDate\n\ninterface ItemRepository: JpaRepository\u003cItem, Long\u003e {\n\n    @Query(\"select i from Item i where i.user = :user and :before \u003c= i.receiptDate and i.receiptDate \u003c= :after\")\n    fun searchItems(@Param(\"user\") user: User, @Param(\"before\") before: LocalDate, @Param(\"after\") after: LocalDate): List\u003cItem\u003e\n}\n","license":"mit"}"""
    lateinit var unitContext: CodeSnippetContext
    lateinit var dump: RawDump

    @BeforeEach
    fun setUp() {
        dump = RawDump.fromString(rawData)
        unitContext = CodeSnippetContext.createUnitContext(dump.toCode())
    }

    @Test
    fun should_identifier_all_imports() {
        val processor = KotlinCodeProcessor(unitContext.rootNode, dump.content)

        val packageNAME = processor.packageName()
        packageNAME shouldBe "jp.ac.kcg.repository"

        val imports = processor.allImports()
        imports.size shouldBe 6
        imports[0] shouldBe "jp.ac.kcg.domain.Item"
        imports[1] shouldBe "jp.ac.kcg.domain.User"
        imports[2] shouldBe "org.springframework.data.jpa.repository.JpaRepository"
        imports[3] shouldBe "org.springframework.data.jpa.repository.Query"
        imports[4] shouldBe "org.springframework.data.repository.query.Param"
        imports[5] shouldBe "java.time.LocalDate"
    }

    @Test
    fun should_get_method_input_type() {
        val processor = KotlinCodeProcessor(unitContext.rootNode, dump.content)

        val nodes = processor.getMethodByAnnotationName("Query")
        nodes.size shouldBe 1

        val inputType = processor.methodParameterType(nodes.first())
        inputType.size shouldBe 3
        inputType[0] shouldBe "User"
        inputType[1] shouldBe "LocalDate"
        inputType[2] shouldBe "LocalDate"

        val fullInputType = processor.methodRequiredTypes(nodes.first(), processor.allImports())
        fullInputType.size shouldBe 3
        fullInputType[0] shouldBe "jp.ac.kcg.domain.User"
        fullInputType[1] shouldBe "java.time.LocalDate"
        fullInputType[2] shouldBe "jp.ac.kcg.domain.Item"
    }

    @Test
    fun should_keep_class_only() {
        val processor = KotlinCodeProcessor(unitContext.rootNode, dump.content)

        val nodes = processor.getMethodByAnnotationName("Query")
        nodes.size shouldBe 1

        val classNode = processor.allClassNodes()
        classNode.size shouldBe 1

        classNode.first().text shouldBe """interface ItemRepository: JpaRepository<Item, Long> {

    @Query("select i from Item i where i.user = :user and :before <= i.receiptDate and i.receiptDate <= :after")
    fun searchItems(@Param("user") user: User, @Param("before") before: LocalDate, @Param("after") after: LocalDate): List<Item>
}"""
    }

    @Test
    fun should_split_methods() {
        val sourceCode =
            """"/*\n * Copyright 2017 Google Inc.\n *\n * Licensed under the Apache License, Version 2.0 (the \"License\");\n * you may not use this file except in compliance with the License.\n * You may obtain a copy of the License at\n *\n *     http://www.apache.org/licenses/LICENSE-2.0\n *\n * Unless required by applicable law or agreed to in writing, software\n * distributed under the License is distributed on an \"AS IS\" BASIS,\n * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n * See the License for the specific language governing permissions and\n * limitations under the License.\n */\n\npackage com.google.android.apps.muzei.gallery\n\nimport android.content.Context\nimport android.content.Intent\nimport android.content.pm.PackageManager\nimport android.net.Uri\nimport android.os.Binder\nimport android.os.Build\nimport android.provider.DocumentsContract\nimport android.util.Log\nimport androidx.lifecycle.LiveData\nimport androidx.paging.PagingSource\nimport androidx.room.Dao\nimport androidx.room.Insert\nimport androidx.room.OnConflictStrategy\nimport androidx.room.Query\nimport androidx.room.Transaction\nimport com.google.android.apps.muzei.api.provider.ProviderContract\nimport com.google.android.apps.muzei.gallery.BuildConfig.GALLERY_ART_AUTHORITY\nimport kotlinx.coroutines.async\nimport kotlinx.coroutines.awaitAll\nimport kotlinx.coroutines.coroutineScope\nimport java.io.File\nimport java.io.FileOutputStream\nimport java.io.IOException\nimport java.util.Date\n\n/**\n * Dao for [ChosenPhoto]\n */\n@Dao\ninternal abstract class ChosenPhotoDao {\n\n    companion object {\n        private const val TAG = \"ChosenPhotoDao\"\n    }\n\n    @get:Query(\"SELECT * FROM chosen_photos ORDER BY _id DESC\")\n    internal abstract val chosenPhotosPaged: PagingSource<Int, ChosenPhoto>\n\n    @get:Query(\"SELECT * FROM chosen_photos ORDER BY _id DESC\")\n    internal abstract val chosenPhotosLiveData: LiveData<List<ChosenPhoto>>\n\n    @get:Query(\"SELECT * FROM chosen_photos ORDER BY _id DESC\")\n    internal abstract val chosenPhotosBlocking: List<ChosenPhoto>\n\n    @Insert(onConflict = OnConflictStrategy.REPLACE)\n    internal abstract suspend fun insertInternal(chosenPhoto: ChosenPhoto): Long\n\n    @Transaction\n    open suspend fun insert(\n            context: Context,\n            chosenPhoto: ChosenPhoto,\n            callingApplication: String?\n    ): Long = if (persistUriAccess(context, chosenPhoto)) {\n        val id = insertInternal(chosenPhoto)\n        if (id != 0L && callingApplication != null) {\n            val metadata = Metadata(ChosenPhoto.getContentUri(id), Date(),\n                    context.getString(R.string.gallery_shared_from, callingApplication))\n            GalleryDatabase.getInstance(context).metadataDao().insert(metadata)\n        }\n        GalleryScanWorker.enqueueInitialScan(context, listOf(id))\n        id\n    } else {\n        0L\n    }\n\n    @Insert(onConflict = OnConflictStrategy.REPLACE)\n    internal abstract suspend fun insertAllInternal(chosenPhoto: List<ChosenPhoto>): List<Long>\n\n    @Transaction\n    open suspend fun insertAll(context: Context, uris: Collection<Uri>) {\n        insertAllInternal(uris\n                .map { ChosenPhoto(it) }\n                .filter { persistUriAccess(context, it) }\n        ).run {\n            if (isNotEmpty()) {\n                GalleryScanWorker.enqueueInitialScan(context, this)\n            }\n        }\n    }\n\n    private fun persistUriAccess(context: Context, chosenPhoto: ChosenPhoto): Boolean {\n        chosenPhoto.isTreeUri = isTreeUri(chosenPhoto.uri)\n        if (chosenPhoto.isTreeUri) {\n            try {\n                context.contentResolver.takePersistableUriPermission(chosenPhoto.uri,\n                        Intent.FLAG_GRANT_READ_URI_PERMISSION)\n            } catch (ignored: SecurityException) {\n                // You can't persist URI permissions from your own app, so this fails.\n                // We'll still have access to it directly\n            }\n        } else {\n            val haveUriPermission = context.checkUriPermission(chosenPhoto.uri,\n                    Binder.getCallingPid(), Binder.getCallingUid(),\n                    Intent.FLAG_GRANT_READ_URI_PERMISSION) == PackageManager.PERMISSION_GRANTED\n            // If we only have permission to this URI via URI permissions (rather than directly,\n            // such as if the URI is from our own app), it is from an external source and we need\n            // to make sure to gain persistent access to the URI's content\n            if (haveUriPermission) {\n                var persistedPermission = false\n                // Try to persist access to the URI, saving us from having to store a local copy\n                if (DocumentsContract.isDocumentUri(context, chosenPhoto.uri)) {\n                    try {\n                        context.contentResolver.takePersistableUriPermission(chosenPhoto.uri,\n                                Intent.FLAG_GRANT_READ_URI_PERMISSION)\n                        persistedPermission = true\n                        // If we have a persisted URI permission, we don't need a local copy\n                        val cachedFile = GalleryProvider.getCacheFileForUri(context, chosenPhoto.uri)\n                        if (cachedFile?.exists() == true) {\n                            if (!cachedFile.delete()) {\n                                Log.w(TAG, \"Unable to delete cachedFile\")\n                            }\n                        }\n                    } catch (ignored: SecurityException) {\n                        // If we don't have FLAG_GRANT_PERSISTABLE_URI_PERMISSION (such as when using ACTION_GET_CONTENT),\n                        // this will fail. We'll need to make a local copy (handled below)\n                    }\n                }\n                if (!persistedPermission) {\n                    // We only need to make a local copy if we weren't able to persist the permission\n                    try {\n                        writeUriToFile(context, chosenPhoto.uri,\n                                GalleryProvider.getCacheFileForUri(context, chosenPhoto.uri))\n                    } catch (e: IOException) {\n                        Log.e(TAG, \"Error downloading gallery image chosenPhoto.uri\", e)\n                        return false\n                    }\n                }\n            }\n        }\n        return true\n    }\n\n    private fun isTreeUri(possibleTreeUri: Uri): Boolean {\n        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {\n            return DocumentsContract.isTreeUri(possibleTreeUri)\n        } else {\n            try {\n                // Prior to N we can't directly check if the URI is a tree URI, so we have to just try it\n                val treeDocumentId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {\n                    DocumentsContract.getTreeDocumentId(possibleTreeUri)\n                } else {\n                    // No tree URIs prior to Lollipop\n                    return false\n                }\n                return treeDocumentId?.isNotEmpty() == true\n            } catch (e: IllegalArgumentException) {\n                // Definitely not a tree URI\n                return false\n            }\n        }\n    }\n\n    @Throws(IOException::class)\n    private fun writeUriToFile(context: Context, uri: Uri, destFile: File?) {\n        if (destFile == null) {\n            throw IOException(\"Invalid destination for uri\")\n        }\n        try {\n            context.contentResolver.openInputStream(uri)?.use { input ->\n                FileOutputStream(destFile).use { out ->\n                    input.copyTo(out)\n                }\n            }\n        } catch (e: SecurityException) {\n            throw IOException(\"Unable to read Uri: uri\", e)\n        } catch (e: UnsupportedOperationException) {\n            throw IOException(\"Unable to read Uri: uri\", e)\n        }\n    }\n\n    @Query(\"SELECT * FROM chosen_photos WHERE _id = :id\")\n    internal abstract fun chosenPhotoBlocking(id: Long): ChosenPhoto?\n\n    @Query(\"SELECT * FROM chosen_photos WHERE _id = :id\")\n    abstract suspend fun getChosenPhoto(id: Long): ChosenPhoto?\n\n    @Query(\"SELECT * FROM chosen_photos WHERE _id IN (:ids)\")\n    abstract suspend fun getChosenPhotos(ids: List<Long>): List<ChosenPhoto>\n\n    @Query(\"DELETE FROM chosen_photos WHERE _id IN (:ids)\")\n    internal abstract suspend fun deleteInternal(ids: List<Long>)\n\n    @Transaction\n    open suspend fun delete(context: Context, ids: List<Long>) {\n        deleteBackingPhotos(context, getChosenPhotos(ids))\n        deleteInternal(ids)\n    }\n\n    @Query(\"DELETE FROM chosen_photos\")\n    internal abstract suspend fun deleteAllInternal()\n\n    @Transaction\n    open suspend fun deleteAll(context: Context) {\n        deleteBackingPhotos(context, chosenPhotosBlocking)\n        deleteAllInternal()\n    }\n\n    /**\n     * We can't just simply delete the rows as that won't free up the space occupied by the\n     * chosen image files for each row being deleted. Instead we have to query\n     * and manually delete each chosen image file\n     */\n    private suspend fun deleteBackingPhotos(\n            context: Context,\n            chosenPhotos: List<ChosenPhoto>\n    ) = coroutineScope  {\n        chosenPhotos.map { chosenPhoto ->\n            async {\n                val contentUri = ProviderContract.getContentUri(GALLERY_ART_AUTHORITY)\n                context.contentResolver.delete(contentUri,\n                        \"${"$"}{ProviderContract.Artwork.METADATA}=?\",\n                        arrayOf(chosenPhoto.uri.toString()))\n                val file = GalleryProvider.getCacheFileForUri(context, chosenPhoto.uri)\n                if (file?.exists() == true) {\n                    if (!file.delete()) {\n                        Log.w(TAG, \"Unable to delete file\")\n                    }\n                } else {\n                    val uriToRelease = chosenPhoto.uri\n                    val contentResolver = context.contentResolver\n                    val haveUriPermission = context.checkUriPermission(uriToRelease,\n                            Binder.getCallingPid(), Binder.getCallingUid(),\n                            Intent.FLAG_GRANT_READ_URI_PERMISSION) == PackageManager.PERMISSION_GRANTED\n                    if (haveUriPermission) {\n                        // Try to release any persisted URI permission for the imageUri\n                        val persistedUriPermissions = contentResolver.persistedUriPermissions\n                        for (persistedUriPermission in persistedUriPermissions) {\n                            if (persistedUriPermission.uri == uriToRelease) {\n                                try {\n                                    contentResolver.releasePersistableUriPermission(\n                                            uriToRelease, Intent.FLAG_GRANT_READ_URI_PERMISSION)\n                                } catch (e: SecurityException) {\n                                    // Thrown if we don't have permission...despite in being in\n                                    // the getPersistedUriPermissions(). Alright then.\n                                }\n                                break\n                            }\n                        }\n                    }\n                }\n            }\n        }.awaitAll()\n    }\n}\n""""
        val newCode =
            """{"repo_name":"romannurik/muzei","path":"source-gallery/src/main/java/com/google/android/apps/muzei/gallery/ChosenPhotoDao.kt","copies":"2","size":"11222","content": $sourceCode,"license":"apache-2.0"}"""
        dump = RawDump.fromString(newCode)
        unitContext = CodeSnippetContext.createUnitContext(dump.toCode())

        val processor = KotlinCodeProcessor(unitContext.rootNode, dump.content)
        val firstClass = processor.allClassNodes().first()
        val newNodes = processor.splitClassMethodsToManyClass(firstClass)
        newNodes.size shouldBe 15
        newNodes.first().text shouldBe """
@Dao
internal abstract class ChosenPhotoDao {

    companion object {
        private const val TAG = "ChosenPhotoDao"
    }

    @get:Query("SELECT * FROM chosen_photos ORDER BY _id DESC")
    internal abstract val chosenPhotosPaged: PagingSource<Int, ChosenPhoto>

    @get:Query("SELECT * FROM chosen_photos ORDER BY _id DESC")
    internal abstract val chosenPhotosLiveData: LiveData<List<ChosenPhoto>>

    @get:Query("SELECT * FROM chosen_photos ORDER BY _id DESC")
    internal abstract val chosenPhotosBlocking: List<ChosenPhoto>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    internal abstract suspend fun insertInternal(chosenPhoto: ChosenPhoto): Long

    
}"""
    }

    val multipleMethodSource =
        """"package com.jamieadkins.gwent.database\n\nimport androidx.room.Dao\nimport androidx.room.Insert\nimport androidx.room.OnConflictStrategy\nimport androidx.room.Query\nimport androidx.room.Transaction\nimport com.jamieadkins.gwent.database.entity.CardEntity\nimport com.jamieadkins.gwent.database.entity.CardWithArtEntity\nimport com.jamieadkins.gwent.domain.GwentFaction\nimport io.reactivex.Flowable\nimport io.reactivex.Single\n\n@Dao\ninterface CardDao {\n\n    @Insert(onConflict = OnConflictStrategy.REPLACE)\n    fun insertCards(items: Collection<CardEntity>)\n\n    @Transaction\n    @Query(\"SELECT * FROM \" + GwentDatabase.CARD_TABLE)\n    fun getCardsOnce(): Single<List<CardWithArtEntity>>\n\n    @Transaction\n    @Query(\"SELECT * FROM \" + GwentDatabase.CARD_TABLE)\n    fun getCards(): Flowable<List<CardWithArtEntity>>\n\n    @Transaction\n    @Query(\"SELECT * FROM \" + GwentDatabase.CARD_TABLE + \" WHERE id=:cardId\")\n    fun getCard(cardId: String): Flowable<CardWithArtEntity>\n\n    @Transaction\n    @Query(\"SELECT * FROM \" + GwentDatabase.CARD_TABLE + \"  WHERE id IN(:ids)\")\n    fun getCards(ids: List<String>): Flowable<List<CardWithArtEntity>>\n\n    @Transaction\n    @Query(\"SELECT * FROM \" + GwentDatabase.CARD_TABLE + \" WHERE faction=:faction AND type='Leader'\")\n    fun getLeaders(faction: String): Flowable<List<CardWithArtEntity>>\n\n    @Transaction\n    @Query(\"SELECT * FROM \" + GwentDatabase.CARD_TABLE + \" WHERE faction IN(:factions) OR secondaryFaction IN(:factions)\")\n    fun getCardsInFactions(factions: List<String>): Flowable<List<CardWithArtEntity>>\n\n    @Query(\"SELECT COUNT(*) FROM \" + GwentDatabase.CARD_TABLE)\n    fun count(): Flowable<Int>\n}\n""""

    @Test
    fun split_by_multiple_methods() {
        val newCode =
            """{"repo_name":"jamieadkins95/Roach","path":"database/src/main/java/com/jamieadkins/gwent/database/CardDao.kt","copies":"1","size":"1647","content":$multipleMethodSource,"license":"apache-2.0"}"""
        dump = RawDump.fromString(newCode)
        unitContext = CodeSnippetContext.createUnitContext(dump.toCode())

        val processor = KotlinCodeProcessor(unitContext.rootNode, dump.content)
        val firstClass = processor.allClassNodes().first()
        val newNodes = processor.splitClassMethodsToManyClass(firstClass)

        newNodes.size shouldBe 8
        newNodes.first().text shouldBe """@Dao
interface CardDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCards(items: Collection<CardEntity>)

    
}"""
    }

    @Test
    fun should_keep_query_only() {
        val newCode =
            """{"repo_name":"jamieadkins95/Roach","path":"database/src/main/java/com/jamieadkins/gwent/database/CardDao.kt","copies":"1","size":"1647","content":$multipleMethodSource,"license":"apache-2.0"}"""
        dump = RawDump.fromString(newCode)
        unitContext = CodeSnippetContext.createUnitContext(dump.toCode())

        val processor = KotlinCodeProcessor(unitContext.rootNode, dump.content)
        val firstClass = processor.allClassNodes().first()
        val queryNodes = processor.splitClassMethodsByAnnotationName(firstClass, "Query")

        queryNodes.size shouldBe 7
        queryNodes.first().text shouldBe """@Dao
interface CardDao {

    

    @Transaction
    @Query("SELECT * FROM " + GwentDatabase.CARD_TABLE)
    fun getCardsOnce(): Single<List<CardWithArtEntity>>

    
}"""

        val insertNodes = processor.splitClassMethodsByAnnotationName(firstClass, "Insert")
        insertNodes.size shouldBe 1
        insertNodes.first().text shouldBe """@Dao
interface CardDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCards(items: Collection<CardEntity>)

    
}"""
    }

    @Test
    fun should_create_constructor_only() {
        val content =
            """"/*\n * Copyright 2017 twitter.com/PensatoAlex\n *\n * Licensed under the Apache License, Version 2.0 (the \"License\");\n * you may not use this file except in compliance with the License.\n * You may obtain a copy of the License at\n *\n *     http://www.apache.org/licenses/LICENSE-2.0\n *\n * Unless required by applicable law or agreed to in writing, software\n * distributed under the License is distributed on an \"AS IS\" BASIS,\n * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n * See the License for the specific language governing permissions and\n * limitations under the License.\n */\npackage net.pensato.data.cassandra.sample.domain\n\nimport org.springframework.cassandra.core.PrimaryKeyType\nimport org.springframework.data.cassandra.mapping.PrimaryKeyColumn\nimport org.springframework.data.cassandra.mapping.Table\n\n@Table\ndata class College(\n        @PrimaryKeyColumn(name = \"name\", ordinal = 1, type = PrimaryKeyType.PARTITIONED)\n        var name: String,\n        var city: String = \"\",\n        var disciplines: Set<String>\n)\n""""
        val rawString =
            """{"repo_name":"romannurik/muzei","path":"source-gallery/src/main/java/com/google/android/apps/muzei/gallery/ChosenPhotoDao.kt","copies":"2","size":"11222","content": $content,"license":"apache-2.0"}"""

        dump = RawDump.fromString(rawString)
        unitContext = CodeSnippetContext.createUnitContext(dump.toCode())

        val processor = KotlinCodeProcessor(unitContext.rootNode, dump.content)
        val allClassNodes = processor.allClassNodes()
        allClassNodes.size shouldBe 1
        allClassNodes.first()
            .classToConstructorText() shouldBe """data class College( @PrimaryKeyColumn(name = "name", ordinal = 1, type = PrimaryKeyType.PARTITIONED) var name: String, var city: String = "", var disciplines: Set<String> )"""
    }

    @Test
    fun should_parse_bean_dao() {
        val content = """{
    "repo_name": "iMeiji/Daily",
    "path": "app/src/main/java/com/meiji/daily/data/local/dao/ZhuanlanDao.kt",
    "copies": "1",
    "size": "774",
    "content": "package com.meiji.daily.data.local.dao\n\n/**\n * Created by Meiji on 2017/11/28.\n */\n\nimport android.arch.persistence.room.Dao\nimport android.arch.persistence.room.Insert\nimport android.arch.persistence.room.OnConflictStrategy\nimport android.arch.persistence.room.Query\n\nimport com.meiji.daily.bean.ZhuanlanBean\n\nimport io.reactivex.Maybe\n\n@Dao\ninterface ZhuanlanDao {\n\n    @Insert(onConflict = OnConflictStrategy.IGNORE)\n    fun insert(zhuanlanBean: ZhuanlanBean): Long\n\n    @Insert(onConflict = OnConflictStrategy.IGNORE)\n    fun insert(list: MutableList<ZhuanlanBean>)\n\n    @Query(\"SELECT * FROM zhuanlans WHERE type = :type\")\n    fun query(type: Int): Maybe<MutableList<ZhuanlanBean>>\n\n    @Query(\"DELETE FROM zhuanlans WHERE slug = :slug\")\n    fun delete(slug: String)\n}\n",
    "license": "apache-2.0"
}"""

        dump = RawDump.fromString(content)
        unitContext = CodeSnippetContext.createUnitContext(dump.toCode())

        val processor = KotlinCodeProcessor(unitContext.rootNode, dump.content)
        val classNodes = processor.allClassNodes()
        classNodes.size shouldBe 1

        val firstNode = classNodes[0]
        val methodNodes = firstNode.allMethods()

        val types = processor.methodRequiredTypes(methodNodes[0], processor.allImports())
        types.size shouldBe 2
        types[0] shouldBe "com.meiji.daily.bean.ZhuanlanBean"
        types[1] shouldBe "Long"
    }
}