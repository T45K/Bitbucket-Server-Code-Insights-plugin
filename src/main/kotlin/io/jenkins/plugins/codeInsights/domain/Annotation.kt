package io.jenkins.plugins.codeInsights.domain

@kotlinx.serialization.Serializable
data class Annotation(
    private val line: Int,
    private val message: String,
    private val path: String,
    private val severity: Severity = Severity.MEDIUM,
)

@kotlinx.serialization.Serializable
enum class Severity {
    LOW, MEDIUM, HIGH
}
