package io.jenkins.plugins.codeInsights.domain

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import io.jenkins.plugins.codeInsights.util.asArray
import io.jenkins.plugins.codeInsights.util.flat
import io.jenkins.plugins.codeInsights.util.toForwardSlashString
import java.nio.file.Paths

class PmdAnnotationProvider(
    private val xmlMapper: XmlMapper,
    private val repositoryPath: String,
    contents: String,
) : AnnotationProvider(contents) {
    override val name: String = "PMD"

    override fun convert(): List<Annotation> {
        val repository = Paths.get(repositoryPath)
        return xmlMapper.readTree(source)["file"].asArray()
            .flatMap { fileTag ->
                val filePath = repository.relativize(Paths.get(fileTag["name"].asText())).toForwardSlashString()
                fileTag["violation"].asArray()
                    .map { violationTag ->
                        val line = violationTag["beginline"].asInt()
                        val message = "$name says: ${violationTag[""].asText().flat()}"
                        val severity = violationTag["priority"].asInt().toAnnotationSeverity()
                        val link = violationTag["externalInfoUrl"].asText()
                        Annotation(line, message, filePath, severity, link)
                    }
            }
    }

    private fun Int.toAnnotationSeverity(): Severity = when (this) {
        1 -> Severity.HIGH
        2, 3 -> Severity.MEDIUM
        4, 5 -> Severity.LOW
        else -> Severity.LOW
    }
}
