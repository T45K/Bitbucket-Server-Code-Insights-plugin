package io.jenkins.plugins.codeInsights.domain

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import io.jenkins.plugins.codeInsights.util.asArray
import io.jenkins.plugins.codeInsights.util.toForwardSlashString
import java.nio.file.Paths

class CheckstyleAnnotationProvider(
    private val xmlMapper: XmlMapper,
    private val repositoryPath: String,
    contents: String,
) : AnnotationProvider(contents) {
    override val name: String = "Checkstyle"

    override fun convert(): List<Annotation> {
        val repository = Paths.get(repositoryPath)
        return xmlMapper.readTree(source)["file"].asArray()
            .flatMap { fileTag ->
                val filePath = repository.relativize(Paths.get(fileTag["name"].asText())).toForwardSlashString()
                fileTag["error"].asArray()
                    .map { errorTag ->
                        val line = errorTag["line"].asInt()
                        val message = "$name says: ${errorTag["message"].asText()}"
                        Annotation(line, message, filePath)
                    }
            }.toList()
    }
}
