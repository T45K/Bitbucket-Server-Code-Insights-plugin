package io.jenkins.plugins.codeInsights.usecase

import hudson.FilePath
import io.jenkins.plugins.codeInsights.JenkinsLogger
import io.jenkins.plugins.codeInsights.domain.ExecutableAnnotationProvidersBuilder
import io.jenkins.plugins.codeInsights.domain.FileTransferService
import io.jenkins.plugins.codeInsights.infrastructure.HttpClient

class AnnotationUsecase(
    private val httpClient: HttpClient,
    private val fileTransferService: FileTransferService,
    private val workspace: FilePath,
    private val checkstyleFilePath: String,
    private val spotBugsFilePath: String,
    private val srcPath: String,
    private val pmdFilePath: String,
    private val sonarQubeUrl: String,
    private val sonarQubeProjectKey: String,
    private val sonarQubeToken: String,
    private val sonarQubeUserName: String,
    private val sonarQubePassword: String,
    private val changedFiles: Set<String>,
) {
    fun execute() {
        httpClient.putReport()

        val executables = ExecutableAnnotationProvidersBuilder(fileTransferService)
            .setCheckstyle(checkstyleFilePath, workspace.remote)
            .setSpotBugs(spotBugsFilePath, srcPath)
            .setPmd(pmdFilePath, workspace.remote)
            .setSonarQube(sonarQubeUrl, sonarQubeProjectKey, sonarQubeToken, sonarQubeUserName, sonarQubePassword)
            .build()
        for (executable in executables) {
            JenkinsLogger.info("Start ${executable.name}")
            val annotations = executable.convert().filter { changedFiles.contains(it.path) }
            if (annotations.isNotEmpty()) {
                httpClient.postAnnotations(executable.name, annotations)
            }
            JenkinsLogger.info("Finish ${executable.name}")
        }
    }
}
