package io.jenkins.plugins.codeInsights.domain

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import io.jenkins.plugins.codeInsights.util.asArray
import java.nio.file.Paths

class SpotBugsAnnotationProvider(
    private val srcPath: String,
    contents: String,
) : AnnotationProvider(contents) {
    override val name: String = "SpotBugs"

    private val xmlMapper = XmlMapper()

    override fun convert(): List<Annotation> =
        xmlMapper.readTree(source)["BugInstance"].asArray().flatMap { bugInstance ->
            val message = bugInstance["LongMessage"].asText()
            bugInstance["SourceLine"].asArray().map { sourceLine ->
                Annotation(
                    sourceLine["start"].asInt(),
                    message,
                    Paths.get(srcPath, sourceLine["sourcePath"].asText()).toString(),
                )
            }
        }
}
