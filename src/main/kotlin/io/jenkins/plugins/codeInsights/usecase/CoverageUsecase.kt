package io.jenkins.plugins.codeInsights.usecase

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import io.jenkins.plugins.codeInsights.JenkinsLogger
import io.jenkins.plugins.codeInsights.domain.FileTransferService
import io.jenkins.plugins.codeInsights.domain.coverage.CoverageProvider
import io.jenkins.plugins.codeInsights.domain.coverage.CoverageRequest
import io.jenkins.plugins.codeInsights.infrastructure.HttpClient

class CoverageUsecase(
    private val fileTransferService: FileTransferService,
    private val jacocoFilePath: String,
    private val srcPath: String,
    private val changedFiles: Set<String>,
    private val httpClient: HttpClient,
) {
    fun execute() {
        JenkinsLogger.info("Coverage enabled")
        JenkinsLogger.info("Start Coverage")
        val coverageRequest = CoverageProvider(fileTransferService, XmlMapper()).convert(jacocoFilePath, srcPath)
            .filter { it.isNotEmpty() }
            .filter { changedFiles.contains(it.path) }
            .let(::CoverageRequest)
        httpClient.postCoverage(coverageRequest)
        JenkinsLogger.info("Finish Coverage")
    }
}
