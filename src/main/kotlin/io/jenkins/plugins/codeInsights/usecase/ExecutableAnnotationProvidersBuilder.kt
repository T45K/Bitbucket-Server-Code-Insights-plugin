package io.jenkins.plugins.codeInsights.usecase

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import io.jenkins.plugins.codeInsights.JenkinsLogger
import io.jenkins.plugins.codeInsights.domain.AnnotationProvider
import io.jenkins.plugins.codeInsights.domain.CheckstyleAnnotationProvider
import io.jenkins.plugins.codeInsights.domain.SonarQubeAnnotationProvider
import io.jenkins.plugins.codeInsights.domain.SonarQubeCredential
import io.jenkins.plugins.codeInsights.domain.SpotBugsAnnotationProvider

class ExecutableAnnotationProvidersBuilder(private val fileTransferService: FileTransferService) {
    private val executables = mutableListOf<AnnotationProvider>()
    private val xmlMapper = XmlMapper()

    fun setCheckstyle(checkstyleFilePath: String, repositoryPath: String): ExecutableAnnotationProvidersBuilder {
        if (checkstyleFilePath.isNotBlank()) {
            JenkinsLogger.info("Checkstyle enabled")
            executables.add(
                CheckstyleAnnotationProvider(
                    xmlMapper,
                    repositoryPath,
                    fileTransferService.readFile(checkstyleFilePath),
                )
            )
        }
        return this
    }

    fun setSpotBugs(spotBugsFilePath: String, srcPath: String): ExecutableAnnotationProvidersBuilder {
        if (spotBugsFilePath.isNotBlank()) {
            JenkinsLogger.info("SpotBugs enabled")
            executables.add(SpotBugsAnnotationProvider(srcPath, fileTransferService.readFile(spotBugsFilePath)))
        }
        return this
    }

    fun setSonarQube(
        sonarQubeUrl: String, sonarQubeProjectKey: String,
        sonarQubeToken: String, sonarQubeUsername: String, sonarQubePassword: String
    ): ExecutableAnnotationProvidersBuilder {
        try {
            val sonarQubeCredential = SonarQubeCredential(sonarQubeToken, sonarQubeUsername, sonarQubePassword)
            JenkinsLogger.info("SonarQube enabled")
            executables.add(SonarQubeAnnotationProvider(sonarQubeUrl, sonarQubeProjectKey, sonarQubeCredential))
        } catch (e: Exception) {
            // Skip
        }
        return this
    }

    fun build(): List<AnnotationProvider> = executables
}
