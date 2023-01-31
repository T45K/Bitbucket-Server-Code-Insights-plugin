package io.jenkins.plugins.codeInsights.usecase

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import io.jenkins.plugins.codeInsights.JenkinsLogger
import io.jenkins.plugins.codeInsights.domain.FileTransferService
import io.jenkins.plugins.codeInsights.domain.coverage.CoverageOverview
import io.jenkins.plugins.codeInsights.domain.coverage.CoverageProvider
import io.jenkins.plugins.codeInsights.domain.coverage.CoverageRequest
import io.jenkins.plugins.codeInsights.infrastructure.HttpClient
import io.jenkins.plugins.codeInsights.infrastructure.dto.ReportData
import io.jenkins.plugins.codeInsights.infrastructure.dto.ReportRequestForBitbucket
import io.jenkins.plugins.codeInsights.infrastructure.dto.ReportType
import io.jenkins.plugins.codeInsights.infrastructure.dto.ResultType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

class CoverageUsecase(
    private val fileTransferService: FileTransferService,
    private val jacocoFilePath: String,
    private val srcPath: String,
    private val changedFiles: Set<String>,
    private val httpClient: HttpClient,
) {
    private val json = Json { encodeDefaults = true }
    private val applicationJsonMediaType = "application/json; charset=utf-8".toMediaType()

    fun execute() {
        JenkinsLogger.info("Coverage enabled")
        JenkinsLogger.info("Start Coverage")
        val coverageRequest = CoverageProvider(fileTransferService, XmlMapper()).convert(jacocoFilePath, srcPath)
            .filter { it.isNotEmpty() }
            .filter { changedFiles.contains(it.path) }
            .let(::CoverageRequest)
        httpClient.postCoverage(coverageRequest)
        JenkinsLogger.info("Finish Coverage")

        JenkinsLogger.info("Start Create Coverage overview")
        val coverageOverviewRequest = CoverageOverview(fileTransferService, XmlMapper()).convert(jacocoFilePath)
        JenkinsLogger.info("End Create Coverage overview")

        JenkinsLogger.info("Prepare payload to report")
        val reportList = mutableListOf<ReportData<String>>()
        reportList.addAll(coverageOverviewRequest)

        val reportRequestBody = json.encodeToString(
            ReportRequestForBitbucket(
                reportList,
                ReportType.COVERAGE,
                ResultType.PASS,
                "Coverage Report by Jenkins."
            )
        ).toRequestBody(applicationJsonMediaType)
        JenkinsLogger.info("Finish prepare payload to report")

        JenkinsLogger.info("Start report to bitbucket")
        httpClient.putReport(reportRequestBody)
        JenkinsLogger.info("Finish report to bitbucket")
    }
}
