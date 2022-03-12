package io.jenkins.plugins.codeInsights.converter

import io.jenkins.plugins.codeInsights.Annotation

interface ResultConverter {
    fun provideAnnotations(
        repositoryPath: String,
        reportKey: String,
    ): List<Annotation>
}
