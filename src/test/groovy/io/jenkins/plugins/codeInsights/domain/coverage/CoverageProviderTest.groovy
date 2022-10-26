package io.jenkins.plugins.codeInsights.domain.coverage

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import io.jenkins.plugins.codeInsights.testUtil.FileUtil
import io.jenkins.plugins.codeInsights.domain.FileTransferService
import spock.lang.Specification

import java.nio.file.Paths

class CoverageProviderTest extends Specification {

    def 'convert '() {
        def fileTransferService = Stub(FileTransferService) {
            readFile('file') >> FileUtil.readString(Paths.get('src', 'test', 'resources', 'jacoco.xml'))
        }
        def sut = new CoverageProvider(fileTransferService, new XmlMapper())

        expect:
        sut.convert('file', 'src/main/java') == [
            new CoverageMeasuredFile('src/main/java/io/jenkins/plugins/codeInsights/domain/AnnotationProvider.kt', 'C:3'),
            new CoverageMeasuredFile('src/main/java/io/jenkins/plugins/codeInsights/domain/Annotation.kt', 'C:6,7,8,9,11,12,14,16;P:5,10'),
            new CoverageMeasuredFile('src/main/java/io/jenkins/plugins/codeInsights/util/JsonNodeUtil.kt', 'P:5')
        ]
    }
}
