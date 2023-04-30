package cc.unitmesh.processor.api.base

interface ApiDetailRender {
    fun render(apiCollections: List<ApiCollection>): String {
        val apiDetails = apiCollections.flatMap { it.items }
        return renderCollection(apiDetails)
    }

    fun renderCollection(apiItems: List<ApiItem>): String

    fun renderByTag(apiItems: List<ApiItem>): List<ApiTagOutput> {
        return apiItems.groupBy { it.tags }.map { (tags, apiDetails) ->
            renderItem(tags, apiDetails)
        }
    }

    fun renderItem(tags: List<String>, apiItems: List<ApiItem>): ApiTagOutput
}
