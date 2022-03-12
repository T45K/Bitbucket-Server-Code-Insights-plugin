package io.jenkins.plugins.codeInsights.annotation

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import io.jenkins.plugins.codeInsights.HttpClient

class AnnotationsProviders private constructor(
    private val executables: List<AnnotationProvider>,
    private val repositoryPath: String,
    private val reportKey: String,
) {
    fun executeAndPost(httpClient: HttpClient) {
        for (executable in executables) {
            val annotations = executable.execute(repositoryPath, reportKey)
            httpClient.postAnnotations(executable.name, annotations)
        }
    }

    class Builder(private val repositoryPath: String, private val reportKey: String) {
        private val list = mutableListOf<AnnotationProvider>()
        private val xmlMapper = XmlMapper()

        fun setCheckstyle(checkstyleFilePath: String?): Builder {
            if (checkstyleFilePath != null) {
                list.add(CheckstyleAnnotationProvider(checkstyleFilePath, xmlMapper))
            }
            return this
        }

        fun build(): AnnotationsProviders = AnnotationsProviders(list, repositoryPath, reportKey)
    }
}
