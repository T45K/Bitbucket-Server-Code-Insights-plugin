package io.jenkins.plugins.codeInsights.domain.coverage

import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import io.jenkins.plugins.codeInsights.domain.FileTransferService
import io.jenkins.plugins.codeInsights.util.asArray
import kotlin.math.roundToInt

class CoverageOverview(
        private val fileTransferService: FileTransferService,
        private val xmlMapper: XmlMapper,
) {

    fun convert(jacocoFilePath: String): List<CoverageOverviewItem<String>> =
            xmlMapper.readTree(fileTransferService.readFile(jacocoFilePath)).asArray().flatMap { reportTag ->
                val counterTags = reportTag["counter"] as ArrayNode

                counterTags.map { tag ->
                    val type = tag["type"].asText()
                    val missed = tag["missed"].asDouble()
                    val covered = tag["covered"].asDouble()
                    val coverage = covered / (missed + covered)
                    val coverageRounded = (coverage * 1000.0).roundToInt() / 10.0

                    CoverageOverviewItem(type, "$coverageRounded %")
                }
            }
}
