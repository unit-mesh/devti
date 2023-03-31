package org.unitmesh.processor

import kotlinx.serialization.Serializable

// scm:
//  - repo: https://github.com/gocd/gocd
//    branch: master
//  - repo: https://github.com/thoughtworks/metrik
//    branch: main
//  - repo: https://github.com/thoughtworks/tramchester
//    branch: master

@Serializable
data class PreProcessorConfig(
    val scm: List<Scm>
)

@Serializable
data class Scm(
    val repository: String,
    val branch: String,
    val language: String
)
