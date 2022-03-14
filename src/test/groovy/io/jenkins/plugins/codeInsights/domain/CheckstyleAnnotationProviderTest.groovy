package io.jenkins.plugins.codeInsights.domain

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Paths

class CheckstyleAnnotationProviderTest extends Specification {

    def 'convert check'() {
        given:
        def sut = new CheckstyleAnnotationProvider(new XmlMapper(), Files.readAllLines(Paths.get("src", "test", "resources", "checkstyle-test.xml")).join("\n"))

        expect:
        sut.convert("/test/repo") == [
            new Annotation(1, "Checkstyle says: message 1",
                Paths.get("src", "main", "java", "A.java").toString(), Severity.MEDIUM),
            new Annotation(2, "Checkstyle says: message 2",
                Paths.get("src", "main", "java", "A.java").toString(), Severity.MEDIUM),
            new Annotation(100, "Checkstyle says: single error",
                Paths.get("src", "main", "java", "B.java").toString(), Severity.MEDIUM)
        ]
    }
}
