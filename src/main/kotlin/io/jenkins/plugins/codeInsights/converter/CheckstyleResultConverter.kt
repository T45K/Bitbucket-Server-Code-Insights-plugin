package io.jenkins.plugins.codeInsights.converter

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import io.jenkins.plugins.codeInsights.Annotation
import java.nio.file.Paths

class CheckstyleResultConverter(
    private val checkstyleFilePath: String,
) : ResultConverter {
    private val xmlMapper = XmlMapper()
    override fun provideAnnotations(repositoryPath: String, reportKey: String): List<Annotation> {
        val repository = Paths.get(repositoryPath)
        val checkStyleFile = repository.resolve(checkstyleFilePath).toFile()
        return xmlMapper.readTree(checkStyleFile)["file"]
            .flatMap { fileTag ->
                val filePath = repository.relativize(Paths.get(fileTag["name"].asText())).toString()
                fileTag["error"].let {
                    if (it.isArray) {
                        it
                    } else {
                        listOf(it)
                    }
                }.map { errorTag ->
                    val line = errorTag["line"].asInt()
                    val message = "checkstyle says: ${errorTag["message"]}"
                    Annotation(
                        reportKey,
                        line,
                        message,
                        filePath,
                    )
                }
            }.toList()
    }
}
