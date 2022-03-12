package io.jenkins.plugins.codeInsights.annotation

interface AnnotationProvider {
    val name: String
    fun execute(repositoryPath: String, reportKey: String): List<Annotation>
}
