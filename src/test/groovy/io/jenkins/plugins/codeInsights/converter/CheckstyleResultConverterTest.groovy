package io.jenkins.plugins.codeInsights.converter

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import io.jenkins.plugins.codeInsights.Annotation
import io.jenkins.plugins.codeInsights.Severity
import spock.lang.Specification

import java.nio.file.Paths

class CheckstyleResultConverterTest extends Specification {

    def 'provideAnnotations check'() {
        given:
        def xmlMapperStub = Stub(XmlMapper) {
            readTree(Paths.get("/test/repo/target/checkstyle-test.xml").toFile())
                >> new XmlMapper().readTree(Paths.get("./src/test/resources/checkstyle-test.xml").toFile())
        }
        def sut = new CheckstyleResultConverter("target/checkstyle-test.xml", xmlMapperStub)

        expect:
        sut.provideAnnotations("/test/repo", "reportKey") == [
            new Annotation("reportKey", 1, "Checkstyle says: \"message 1\"",
                Paths.get("src", "main", "java", "A.java").toString(), Severity.MEDIUM),
            new Annotation("reportKey", 2, "Checkstyle says: \"message 2\"",
                Paths.get("src", "main", "java", "A.java").toString(), Severity.MEDIUM),
            new Annotation("reportKey", 100, "Checkstyle says: \"single error\"",
                Paths.get("src", "main", "java", "B.java").toString(), Severity.MEDIUM)
        ]
    }
}
