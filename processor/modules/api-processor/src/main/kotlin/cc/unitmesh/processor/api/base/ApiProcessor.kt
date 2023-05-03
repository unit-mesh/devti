package cc.unitmesh.processor.api.base

import cc.unitmesh.core.model.ApiCollection

interface ApiProcessor {
    fun convertApi(): List<ApiCollection>
}