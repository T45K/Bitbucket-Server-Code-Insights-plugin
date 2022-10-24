package io.jenkins.plugins.codeInsights.domain.coverage

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import io.jenkins.plugins.codeInsights.testUtil.FileUtil
import io.jenkins.plugins.codeInsights.usecase.FileTransferService
import spock.lang.Specification

import java.nio.file.Paths

class CoverageProviderTest extends Specification {

    def 'convert '() {
        def fileTransferService = Stub(FileTransferService) {
            readFile(*_) >> FileUtil.readString(Paths.get('src', 'test', 'resources', 'jacoco.xml'))
        }
        def sut = new CoverageProvider(fileTransferService, new XmlMapper())

        expect:
        println sut.convert("sss", "src/main/java")
        // TODO: write assertion
    }
}
