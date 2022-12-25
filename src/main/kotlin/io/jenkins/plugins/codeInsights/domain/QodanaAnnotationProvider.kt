package io.jenkins.plugins.codeInsights.domain

class QodanaAnnotationProvider(source: String) : AnnotationProvider(source) {

    override val name: String
        get() = "Qodana"

    override fun convert(): List<Annotation> {

        TODO("Not yet implemented")
    }
}
