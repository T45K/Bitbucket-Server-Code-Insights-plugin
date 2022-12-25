package io.jenkins.plugins.codeInsights.usecase

import io.jenkins.plugins.codeInsights.JenkinsLogger
import io.jenkins.plugins.codeInsights.domain.AnnotationProvider
import io.jenkins.plugins.codeInsights.infrastructure.HttpClient

class AnnotationUsecase(
    private val httpClient: HttpClient,
    private val executables: List<AnnotationProvider>,
    private val changedFiles: Set<String>,
) {
    fun execute() {
        httpClient.putReport()

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
