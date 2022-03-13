package io.jenkins.plugins.codeInsights.annotation

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import hudson.FilePath
import io.jenkins.plugins.codeInsights.HttpClient
import io.jenkins.plugins.codeInsights.JenkinsLogger

class AnnotationsProviders private constructor(
    private val executables: List<AnnotationProvider>,
    private val workspace: FilePath,
) {
    fun executeAndPost(httpClient: HttpClient) {
        for (executable in executables) {
            JenkinsLogger.info("Start ${executable.name}")
            val annotations = executable.execute(workspace)
            httpClient.postAnnotations(executable.name, annotations)
            JenkinsLogger.info("Finish ${executable.name}")
        }
    }

    class Builder(private val workspace: FilePath) {
        private val list = mutableListOf<AnnotationProvider>()
        private val xmlMapper = XmlMapper()

        fun setCheckstyle(checkstyleFilePath: String?): Builder {
            if (checkstyleFilePath != null) {
                JenkinsLogger.info("Checkstyle enabled")
                list.add(CheckstyleAnnotationProvider(xmlMapper, checkstyleFilePath))
            }
            return this
        }

        fun build(): AnnotationsProviders = AnnotationsProviders(list, workspace)
    }
}
