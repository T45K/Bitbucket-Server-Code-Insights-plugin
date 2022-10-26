package io.jenkins.plugins.codeInsights.domain.coverage

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import io.jenkins.plugins.codeInsights.usecase.FileTransferService
import io.jenkins.plugins.codeInsights.util.asArray
import io.jenkins.plugins.codeInsights.util.toForwardSlashString
import java.nio.file.Paths
import java.util.StringJoiner

class CoverageProvider(
    private val fileTransferService: FileTransferService,
    private val xmlMapper: XmlMapper,
) {

    fun convert(jacocoFilePath: String, srcPath: String): List<CoverageMeasuredFiles> =
        xmlMapper.readTree(fileTransferService.readFile(jacocoFilePath))["package"].asArray().flatMap { packageTag ->
            val packageName = packageTag["name"].asText()
            packageTag["sourcefile"].asArray().map { sourceFile ->
                val sourceFileName = sourceFile["name"].asText()
                val coverageGenerator = CoverageGenerator()
                for (coverageTag in sourceFile["line"].asArray()) {
                    val line = coverageTag["nr"].asInt()
                    val coverageInfo = CoverageInfo(
                        coverageTag["mi"].asInt(),
                        coverageTag["ci"].asInt(),
                        coverageTag["mb"].asInt(),
                        coverageTag["cb"].asInt(),
                    )
                    when {
                        coverageInfo.isCovered() -> coverageGenerator.addCoveredLines(line)
                        coverageInfo.isPartialCovered() -> coverageGenerator.addPartialCoveredLines(line)
                        coverageInfo.isUncovered() -> coverageGenerator.addUncoveredLines(line)
                    }
                }
                val path = Paths.get(srcPath, packageName, sourceFileName).toForwardSlashString()
                CoverageMeasuredFiles(path, coverageGenerator.toString())
            }
        }
}

class CoverageInfo(
    private val missedInstruction: Int,
    private val coveredInstruction: Int,
    private val missedBranch: Int,
    private val coveredBranch: Int,
) {
    fun isCovered(): Boolean =
        isStatement() && missedInstruction == 0 ||
            isBranch() && missedBranch == 0

    fun isPartialCovered() = !isBranch() && !isUncovered()

    fun isUncovered(): Boolean =
        isStatement() && coveredInstruction == 0 ||
            isBranch() && coveredBranch == 0

    private fun isStatement(): Boolean = missedBranch == 0 && coveredBranch == 0

    private fun isBranch(): Boolean = !isStatement()
}

class CoverageGenerator {
    private val coveredLines = mutableListOf<Int>()
    private val partialCoveredLines = mutableListOf<Int>()
    private val uncoveredLines = mutableListOf<Int>()

    fun addCoveredLines(line: Int) {
        coveredLines += line
    }

    fun addPartialCoveredLines(line: Int) {
        partialCoveredLines += line
    }

    fun addUncoveredLines(line: Int) {
        uncoveredLines += line
    }

    override fun toString(): String {
        val joiner = StringJoiner(";")
        if (coveredLines.isNotEmpty()) {
            joiner.add("C:${coveredLines.joinToString(",")}")
        }
        if (partialCoveredLines.isNotEmpty()) {
            joiner.add("P:${partialCoveredLines.joinToString(",")}")
        }
        if (uncoveredLines.isNotEmpty()) {
            joiner.add("U:${uncoveredLines.joinToString(",")}")
        }
        return joiner.toString()
    }
}
