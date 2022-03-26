package io.jenkins.plugins.codeInsights.domain

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import io.jenkins.plugins.codeInsights.testUtil.FileUtil
import spock.lang.Specification

import java.nio.file.Paths

class PmdAnnotationProviderTest extends Specification {

    def 'convert returns annotation based on PMD result file'() {
        given:
        final def sut = new PmdAnnotationProvider(
            new XmlMapper(),
            '/test/repo',
            FileUtil.readString(Paths.get('src', 'test', 'resources', 'pmd-test.xml')),
        )

        expect:
        sut.convert() == [
            new Annotation(19, 'PMD says: Avoid unused local variables such as \'a\'.',
                'src/main/java/io/jenkins/plugins/codeInsights/Sample.java', Severity.MEDIUM, 'https://pmd.github.io/pmd-6.42.0/pmd_rules_java_bestpractices.html#unusedlocalvariable')
        ]
    }
}
