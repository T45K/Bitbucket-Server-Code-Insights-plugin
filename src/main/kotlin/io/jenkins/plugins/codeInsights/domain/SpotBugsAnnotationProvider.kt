package io.jenkins.plugins.codeInsights.domain

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import io.jenkins.plugins.codeInsights.util.asArray
import io.jenkins.plugins.codeInsights.util.flat
import io.jenkins.plugins.codeInsights.util.toForwardSlashString
import java.nio.file.Paths

class SpotBugsAnnotationProvider(
    private val srcPath: String,
    private val xmlMapper: XmlMapper,
    contents: String,
) : AnnotationProvider(contents) {
    override val name: String = "SpotBugs"

    override fun convert(): List<Annotation> =
        xmlMapper.readTree(source)["BugInstance"].asArray().flatMap { bugInstance ->
            val message = bugInstance["LongMessage"].asText().flat()
            bugInstance["SourceLine"].asArray().map { sourceLine ->
                Annotation(
                    sourceLine["start"]?.asInt() ?: 0,
                    message,
                    Paths.get(srcPath, sourceLine["sourcepath"].asText()).toForwardSlashString(),
                )
            }
        }
}
