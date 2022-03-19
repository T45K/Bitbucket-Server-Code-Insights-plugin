package io.jenkins.plugins.codeInsights.domain

abstract class AnnotationProvider(protected val source: String) {
    abstract val name: String

    abstract fun convert(repositoryPath: String): List<Annotation>
}
