package cc.unitmesh.processor.api.swagger

import cc.unitmesh.processor.api.model.ApiDetails

interface SwaggerProcessor {
    fun convertApi(): List<ApiDetails>
}