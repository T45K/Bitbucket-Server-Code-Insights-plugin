package io.jenkins.plugins.codeInsights.domain

import hudson.FilePath
import java.io.ByteArrayOutputStream

abstract class AnnotationProvider(private val resultFilePath: String) {
    abstract val name: String
    fun execute(workspace: FilePath): List<Annotation> {
        val contents = readFile(workspace, resultFilePath)
        return convert(workspace.absolutize().remote, contents)
    }

    abstract fun convert(repositoryPath: String, contents: String): List<Annotation>

    private fun readFile(workspace: FilePath, resultFilePath: String): String =
        ByteArrayOutputStream()
            .also { workspace.child(resultFilePath).copyTo(it) }
            .toString()
}
