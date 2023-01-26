package io.jenkins.plugins.codeInsights.domain.coverage

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import io.jenkins.plugins.codeInsights.testUtil.FileUtil
import io.jenkins.plugins.codeInsights.domain.FileTransferService
import spock.lang.Specification

import java.nio.file.Paths

class CoverageOverviewTest extends Specification {

    def 'convert '() {
        def fileTransferService = Stub(FileTransferService) {
            readFile('file') >> FileUtil.readString(Paths.get('src', 'test', 'resources', 'jacoco.xml'))
        }
        def sut = new CoverageOverview(fileTransferService, new XmlMapper())

        expect:
        sut.convert('file') == [
                new CoverageOverviewItem("INSTRUCTION", "0.09 %"),
                new CoverageOverviewItem("BRANCH", "0.28 %"),
                new CoverageOverviewItem("LINE", "0.0 %"),
                new CoverageOverviewItem("COMPLEXITY", "0.19 %"),
                new CoverageOverviewItem("METHOD", "0.02 %"),
                new CoverageOverviewItem("CLASS", "0.0 %"),
        ]
    }
}
