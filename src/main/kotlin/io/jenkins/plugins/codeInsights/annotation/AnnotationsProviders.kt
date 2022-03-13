package io.jenkins.plugins.codeInsights.annotation

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import hudson.FilePath
import io.jenkins.plugins.codeInsights.HttpClient

class AnnotationsProviders private constructor(
    private val executables: List<AnnotationProvider>,
    private val workspace: FilePath,
) {
    fun executeAndPost(httpClient: HttpClient) {
        for (executable in executables) {
            val annotations = executable.execute(workspace)
            httpClient.postAnnotations(executable.name, annotations)
        }
    }

    class Builder(private val workspace: FilePath) {
        private val list = mutableListOf<AnnotationProvider>()
        private val xmlMapper = XmlMapper()

        fun setCheckstyle(checkstyleFilePath: String?): Builder {
            if (checkstyleFilePath != null) {
                list.add(CheckstyleAnnotationProvider(xmlMapper, checkstyleFilePath))
            }
            return this
        }

        fun build(): AnnotationsProviders = AnnotationsProviders(list, workspace)
    }
}
