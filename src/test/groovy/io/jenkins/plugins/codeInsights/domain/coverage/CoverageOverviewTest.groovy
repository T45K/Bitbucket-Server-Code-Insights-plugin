package io.jenkins.plugins.codeInsights.domain.coverage

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import io.jenkins.plugins.codeInsights.domain.FileTransferService
import spock.lang.Specification

import java.nio.file.Path

class CoverageOverviewTest extends Specification {

    def 'Calculate coverage from jacoco.xml'() {
        def fileTransferService = Stub(FileTransferService) {
            readFile('file') >> Path.of('src', 'test', 'resources', 'jacoco.xml').text
        }
        def sut = new CoverageOverview(fileTransferService, new XmlMapper())

        expect:
        sut.convert('file') == [
            new CoverageOverviewItem("INSTRUCTION", "90.9 %"),
            new CoverageOverviewItem("BRANCH", "72.3 %"),
            new CoverageOverviewItem("LINE", "99.7 %"),
            new CoverageOverviewItem("COMPLEXITY", "81.0 %"),
            new CoverageOverviewItem("METHOD", "97.8 %"),
            new CoverageOverviewItem("CLASS", "100.0 %"),
        ]
    }
}
