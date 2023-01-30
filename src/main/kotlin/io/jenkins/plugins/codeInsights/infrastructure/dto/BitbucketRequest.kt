package io.jenkins.plugins.codeInsights.infrastructure.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Report request data.
 * @see <a href="https://developer.atlassian.com/cloud/bitbucket/rest/api-group-reports/#api-repositories-workspace-repo-slug-commit-commit-reports-reportid-put">Official doc</a>
 */
@Serializable
data class ReportRequestForBitbucket<T>(
    val data: List<ReportData<T>>,
    @SerialName("report_type")
    val reportType: ReportType,
    val result: ResultType,
    val details: String,
    val title: String = "Jenkins Report",
    val reporter: String = "Jenkins",
    val logoUrl: String = "https://www.jenkins.io/images/logos/superhero/256.png",
)

@Serializable
enum class ReportType {
    SECURITY, COVERAGE, TEST, BUG,
}

@Serializable
enum class ResultType {
    PASS, FAIL,
}

@Serializable
data class ReportData<T>(
    val title: String,
    val type: ReportDataType,
    val value: T,
)

@Serializable
enum class ReportDataType {
    DURATION, BOOLEAN, Omitted, DATE, LINK, NUMBER, PERCENTAGE, TEXT
}
