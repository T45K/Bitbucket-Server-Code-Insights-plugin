package io.jenkins.plugins.codeInsights.domain

import kotlinx.serialization.Serializable

@Serializable
data class Annotation(
    private val line: Int,
    private val message: String,
    val path: String,
    private val severity: Severity = Severity.LOW,
    private val link: String? = null,
)

@Serializable
enum class Severity {
    LOW, MEDIUM, HIGH
}
