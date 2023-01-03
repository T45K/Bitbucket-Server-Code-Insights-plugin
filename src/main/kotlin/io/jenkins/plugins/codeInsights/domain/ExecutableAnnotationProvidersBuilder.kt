package io.jenkins.plugins.codeInsights.domain

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import io.jenkins.plugins.codeInsights.JenkinsLogger

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
                    fileTransferService.readFile(checkstyleFilePath)
                )
            )
        }
        return this
    }

    fun setSpotBugs(spotBugsFilePath: String, srcPath: String): ExecutableAnnotationProvidersBuilder {
        if (spotBugsFilePath.isNotBlank()) {
            JenkinsLogger.info("SpotBugs enabled")
            executables.add(
                SpotBugsAnnotationProvider(
                    srcPath,
                    xmlMapper,
                    fileTransferService.readFile(spotBugsFilePath)
                )
            )
        }
        return this
    }

    fun setPmd(pmdFilePath: String, repositoryPath: String): ExecutableAnnotationProvidersBuilder {
        if (pmdFilePath.isNotBlank()) {
            JenkinsLogger.info("PMD enabled")
            executables.add(PmdAnnotationProvider(xmlMapper, repositoryPath, fileTransferService.readFile(pmdFilePath)))
        }
        return this
    }

    fun setQodana(qodanaFilePath: String): ExecutableAnnotationProvidersBuilder {
        if (qodanaFilePath.isNotBlank()) {
            JenkinsLogger.info("Qodana enabled")
            executables.add(QodanaAnnotationProvider(fileTransferService.readFile(qodanaFilePath)))
        }
        return this
    }

    fun setSonarQube(
        sonarQubeUrl: String, sonarQubeProjectKey: String,
        sonarQubeToken: String, sonarQubeUsername: String, sonarQubePassword: String
    ): ExecutableAnnotationProvidersBuilder {
        try {
            val sonarQubeCredential = SonarQubeCredential(sonarQubeToken, sonarQubeUsername, sonarQubePassword)
            if (sonarQubeUrl.isNotBlank() && sonarQubeProjectKey.isNotBlank()) {
                JenkinsLogger.info("SonarQube enabled")
                executables.add(SonarQubeAnnotationProvider(sonarQubeUrl, sonarQubeProjectKey, sonarQubeCredential))
            }
        } catch (e: Exception) {
            JenkinsLogger.info(e.message ?: "")
        }
        return this
    }

    fun build(): List<AnnotationProvider> = executables
}
