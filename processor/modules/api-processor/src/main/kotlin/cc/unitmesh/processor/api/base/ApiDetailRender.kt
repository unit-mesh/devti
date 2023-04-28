package cc.unitmesh.processor.api.base

interface ApiDetailRender {
    fun render(apiDetails: List<ApiDetail>): String

    fun renderByTag(apiDetails: List<ApiDetail>): List<ApiTagOutput> {
        return apiDetails.groupBy { it.tags }.map { (tags, apiDetails) ->
            renderItem(tags, apiDetails)
        }
    }

    fun renderItem(tags: List<String>, apiDetails: List<ApiDetail>): ApiTagOutput
}
