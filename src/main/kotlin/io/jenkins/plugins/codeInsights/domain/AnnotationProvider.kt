package io.jenkins.plugins.codeInsights.domain

abstract class AnnotationProvider(protected val contents: String) {
    abstract val name: String

    abstract fun convert(repositoryPath: String): List<Annotation>
}
