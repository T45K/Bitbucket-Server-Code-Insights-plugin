package io.jenkins.plugins.codeInsights.domain

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class QodanaAnnotationProvider(content: String) : AnnotationProvider(content) {

    override val name: String
        get() = "Qodana"

    private val json = Json { ignoreUnknownKeys = true }

    override fun convert(): List<Annotation> =
        json.decodeFromString<ReportStructure>(source).runs
            .flatMap { it.results }
            .mapNotNull {
                val severity = when (it.level) {
                    "error" -> Severity.HIGH
                    "warning" -> Severity.MEDIUM
                    "note",
                    else -> Severity.LOW
                }
                if (it.locations.isEmpty()) {
                    return@mapNotNull null
                }
                val location = it.locations.first()
                Annotation(
                    location.physicalLocation.region.startLine,
                    it.message.text,
                    location.physicalLocation.artifactLocation.uri,
                    severity
                )
            }
}

// https://www.jetbrains.com/help/qodana/qodana-sarif-output.html
@Serializable
data class ReportStructure(val runs: List<RunStructure>)

@Serializable
data class RunStructure(val results: List<ResultStructure>)

// https://www.jetbrains.com/help/qodana/qodana-sarif-output.html#results
@Serializable
data class ResultStructure(
    val level: String,
    val message: Message,
    val locations: List<LocationStructure>,
) {
    @Serializable
    data class Message(val text: String)
}

// https://docs.oasis-open.org/sarif/sarif/v2.1.0/os/sarif-v2.1.0-os.html#_Toc34317670
@Serializable
data class LocationStructure(val physicalLocation: PhysicalLocation)

@Serializable
data class PhysicalLocation(
    val artifactLocation: ArtifactLocation,
    val region: Region,
) {
    @Serializable
    data class ArtifactLocation(val uri: String)

    @Serializable
    data class Region(val startLine: Int)
}
