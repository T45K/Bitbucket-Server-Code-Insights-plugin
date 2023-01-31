package io.jenkins.plugins.codeInsights.domain.coverage

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import io.jenkins.plugins.codeInsights.domain.FileTransferService
import io.jenkins.plugins.codeInsights.infrastructure.dto.ReportData
import io.jenkins.plugins.codeInsights.infrastructure.dto.ReportDataType
import io.jenkins.plugins.codeInsights.testUtil.FileUtil
import spock.lang.Specification

import java.nio.file.Path

class CoverageOverviewTest extends Specification {

    def 'Calculate coverage from jacoco.xml'() {
        def fileTransferService = Stub(FileTransferService) {
            readFile('file') >> FileUtil.readString(Path.of('src', 'test', 'resources', 'jacoco.xml'))
        }
        def sut = new CoverageOverview(fileTransferService, new XmlMapper())

        expect:
        sut.convert('file') == [
                new ReportData("INSTRUCTION", ReportDataType.TEXT, "90.9 %"),
                new ReportData("BRANCH", ReportDataType.TEXT, "72.3 %"),
                new ReportData("LINE", ReportDataType.TEXT, "99.7 %"),
                new ReportData("COMPLEXITY", ReportDataType.TEXT, "81.0 %"),
                new ReportData("METHOD", ReportDataType.TEXT, "97.8 %"),
                new ReportData("CLASS", ReportDataType.TEXT, "100.0 %"),
        ]
    }
}
