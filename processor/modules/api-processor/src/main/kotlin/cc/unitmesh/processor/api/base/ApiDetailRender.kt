package cc.unitmesh.processor.api.base

interface ApiDetailRender {
    fun render(apiItems: List<ApiItem>): String

    fun renderByTag(apiItems: List<ApiItem>): List<ApiTagOutput> {
        return apiItems.groupBy { it.tags }.map { (tags, apiDetails) ->
            renderItem(tags, apiDetails)
        }
    }

    fun renderItem(tags: List<String>, apiItems: List<ApiItem>): ApiTagOutput
}
