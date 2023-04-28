package cc.unitmesh.processor.api.base

interface ApiDetailRender {
    fun render(apiDetails: List<ApiDetails>): String
}