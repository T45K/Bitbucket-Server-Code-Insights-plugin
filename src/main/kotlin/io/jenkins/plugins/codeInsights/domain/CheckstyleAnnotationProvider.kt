package io.jenkins.plugins.codeInsights.domain

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import java.nio.file.Paths

class CheckstyleAnnotationProvider(
    private val xmlMapper: XmlMapper,
    checkstyleFilePath: String,
) : AnnotationProvider(checkstyleFilePath) {
    override val name: String = "Checkstyle"

    override fun convert(repositoryPath: String, contents: String): List<Annotation> {
        val repository = Paths.get(repositoryPath)
        return xmlMapper.readTree(contents)["file"]
            .flatMap { fileTag ->
                val filePath = repository.relativize(Paths.get(fileTag["name"].asText())).toString()
                fileTag["error"]
                    .let { if (it.isArray) it else listOf(it) }
                    .map { errorTag ->
                        val line = errorTag["line"].asInt()
                        val message = "$name says: ${errorTag["message"].asText()}"
                        Annotation(line, message, filePath)
                    }
            }.toList()
    }
}
