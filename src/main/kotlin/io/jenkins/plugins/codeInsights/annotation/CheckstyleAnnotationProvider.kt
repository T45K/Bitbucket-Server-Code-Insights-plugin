package io.jenkins.plugins.codeInsights.annotation

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import java.nio.file.Paths

class CheckstyleAnnotationProvider(
    private val checkstyleFilePath: String,
    private val xmlMapper: XmlMapper,
) : AnnotationProvider {
    override val name: String = "Checkstyle"

    override fun execute(repositoryPath: String, reportKey: String): List<Annotation> {
        val repository = Paths.get(repositoryPath)
        val checkStyleFile = repository.resolve(checkstyleFilePath).toFile()
        return xmlMapper.readTree(checkStyleFile)["file"]
            .flatMap { fileTag ->
                val filePath = repository.relativize(Paths.get(fileTag["name"].asText())).toString()
                fileTag["error"]
                    .let { if (it.isArray) it else listOf(it) }
                    .map { errorTag ->
                        val line = errorTag["line"].asInt()
                        val message = "Checkstyle says: ${errorTag["message"].asText()}"
                        Annotation(reportKey, line, message, filePath)
                    }
            }.toList()
    }
}
