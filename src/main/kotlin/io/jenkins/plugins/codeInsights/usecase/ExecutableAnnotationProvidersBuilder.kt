package io.jenkins.plugins.codeInsights.usecase

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import io.jenkins.plugins.codeInsights.JenkinsLogger
import io.jenkins.plugins.codeInsights.domain.AnnotationProvider
import io.jenkins.plugins.codeInsights.domain.CheckstyleAnnotationProvider

class ExecutableAnnotationProvidersBuilder(private val fileTransferService: FileTransferService) {
    private val executables = mutableListOf<AnnotationProvider>()
    private val xmlMapper = XmlMapper()

    fun setCheckstyle(checkstyleFilePath: String): ExecutableAnnotationProvidersBuilder {
        if (checkstyleFilePath.isNotBlank()) {
            JenkinsLogger.info("Checkstyle enabled")
            executables.add(CheckstyleAnnotationProvider(xmlMapper, fileTransferService.readFile(checkstyleFilePath)))
        }
        return this
    }

    fun build(): List<AnnotationProvider> = executables
}
