package io.jenkins.plugins.codeInsights.usecase

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import io.jenkins.plugins.codeInsights.JenkinsLogger
import io.jenkins.plugins.codeInsights.domain.FileTransferService
import io.jenkins.plugins.codeInsights.domain.coverage.CoverageOverview
import io.jenkins.plugins.codeInsights.domain.coverage.CoverageProvider
import io.jenkins.plugins.codeInsights.domain.coverage.CoverageRequest
import io.jenkins.plugins.codeInsights.infrastructure.HttpClient
import io.jenkins.plugins.codeInsights.infrastructure.dto.ReportRequestForBitbucket
import io.jenkins.plugins.codeInsights.infrastructure.dto.ReportType
import io.jenkins.plugins.codeInsights.infrastructure.dto.ResultType
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

class CoverageUsecase(
    private val fileTransferService: FileTransferService,
    private val jacocoFilePath: String,
    private val srcPath: String,
    private val changedFiles: Set<String>,
    private val reportKey: String,
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

        JenkinsLogger.info("Start Coverage overview post")
        val coverageOverviewRequest = CoverageOverview(fileTransferService, XmlMapper()).convert(jacocoFilePath)
        val reportRequestBody = json.encodeToString(
            ReportRequestForBitbucket(coverageOverviewRequest, ReportType.COVERAGE, ResultType.PASS, "")
        ).toRequestBody(applicationJsonMediaType)
        httpClient.putReport(reportRequestBody, "$reportKey _ coverage")
        JenkinsLogger.info("Finish Coverage overview")
    }
}
