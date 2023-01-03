package io.jenkins.plugins.codeInsights.domain

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import spock.lang.Specification

import java.nio.file.Path

@SuppressWarnings('GroovyAccessibility')
class PmdAnnotationProviderTest extends Specification {

    def 'convert returns annotation based on PMD result file'() {
        given:
        final def sut = new PmdAnnotationProvider(
            new XmlMapper(),
            '/test/repo',
            Path.of('src', 'test', 'resources', 'pmd-test.xml').text,
        )

        expect:
        sut.convert() == [
            new Annotation(19, 'PMD says: Avoid unused local variables such as \'a\'.',
                'src/main/java/Sample.java', Severity.MEDIUM, 'https://pmd.github.io/pmd-6.42.0/pmd_rules_java_bestpractices.html#unusedlocalvariable')
        ]
    }

    def 'toAnnotationSeverity converts int value to Severity'() {
        given:
        final def sut = new PmdAnnotationProvider(
            new XmlMapper(),
            '/test/repo',
            Path.of('src', 'test', 'resources', 'pmd-test.xml').text,
        )

        expect:
        sut.toAnnotationSeverity(input) == output

        where:
        input || output
        1     || Severity.HIGH
        2     || Severity.MEDIUM
        3     || Severity.MEDIUM
        4     || Severity.LOW
        5     || Severity.LOW
        10000 || Severity.LOW
    }
}
