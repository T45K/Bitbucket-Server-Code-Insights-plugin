package io.jenkins.plugins.codeInsights.domain.coverage

import kotlinx.serialization.Serializable

@Serializable
data class Coverage(
    private val files: List<CoverageMeasuredFiles>
)

@Serializable
data class CoverageMeasuredFiles(
    val path: String,
    private val coverage: String,
) {
    fun isNotEmpty(): Boolean = coverage.isNotEmpty()
}
