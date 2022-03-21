package io.jenkins.plugins.codeInsights.domain

class SpotBugsAnnotationProvider(
    spotBugsFilePath: String,
    srcPath: String,
) : AnnotationProvider(spotBugsFilePath) {
    override val name: String = "SpotBugs"

    override fun convert(): List<Annotation> {
        TODO("Not yet implemented")
    }
}
