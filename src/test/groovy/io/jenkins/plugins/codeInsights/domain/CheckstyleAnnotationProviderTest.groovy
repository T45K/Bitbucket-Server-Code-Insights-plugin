package io.jenkins.plugins.codeInsights.domain

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import spock.lang.Specification

import java.nio.file.Path

class CheckstyleAnnotationProviderTest extends Specification {

    def 'convert returns annotations based on checkstyle result file'() {
        given:
        final def sut = new CheckstyleAnnotationProvider(
            new XmlMapper(),
            '/test/repo',
            Path.of('src', 'test', 'resources', 'checkstyle-test.xml').text
        )

        expect:
        sut.convert() == [
            new Annotation(1, 'Checkstyle says: message 1', 'src/main/java/A.java', Severity.LOW, null),
            new Annotation(2, 'Checkstyle says: message 2', 'src/main/java/A.java', Severity.LOW, null),
            new Annotation(100, 'Checkstyle says: single error', 'src/main/java/B.java', Severity.LOW, null),
        ]
    }
}
