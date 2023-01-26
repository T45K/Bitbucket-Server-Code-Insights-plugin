package io.jenkins.plugins.codeInsights.domain.coverage

import kotlinx.serialization.Serializable

@Serializable
data class CoverageRequest(
    private val files: List<CoverageMeasuredFile>
)

@Serializable
data class CoverageMeasuredFile(
    val path: String,
    private val coverage: String,
) {
    fun isNotEmpty(): Boolean = coverage.isNotEmpty()
}

@Serializable
data class CoverageOverviewItem<T>(
    val title: String,
    val value: T,
)
