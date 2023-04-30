package cc.unitmesh.processor.api.base

interface ApiDetailRender {
    fun render(apiCollections: List<ApiCollection>): String {
        val apiDetailsByTag = apiCollections.map { renderCollection(it) }
        return apiDetailsByTag.joinToString("\n\n") { it }
    }

    fun renderCollection(collection: ApiCollection): String

    fun renderItem(apiItems: List<ApiItem>): ApiTagOutput
}
