package org.unitmesh.processor.swagger.converter

import org.unitmesh.processor.swagger.ApiDetails

interface SwaggerProcessor {
    fun convertApi(): List<ApiDetails>
}