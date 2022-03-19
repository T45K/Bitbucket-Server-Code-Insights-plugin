package io.jenkins.plugins.codeInsights.domain

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import spock.lang.Specification

import static io.jenkins.plugins.codeInsights.util.SonarQubeResponseHereDocument.RESPONSE_BODY

class SonarQubeAnnotationProviderTest extends Specification {

    private final def server = new MockWebServer()

    def 'convert returns annotations'() {
        given:
        server.enqueue(new MockResponse().setBody(RESPONSE_BODY)) // call to fetch total page
        server.enqueue(new MockResponse().setBody(RESPONSE_BODY)) // call to fetch issue

        final def sut = new SonarQubeAnnotationProvider(server.url('').toString(), 'trial', new SonarQubeCredential('', 'admin', 'admin'))

        expect:
        sut.convert('') == [
            new Annotation(1, 'SonarQube says: Rename this package name to match the regular expression \'^[a-z_]+(\\.[a-z_][a-z0-9_]*)*$\'.',
                'src/main/java/io/jenkins/plugins/codeInsights/CodeInsightsBuilder.java', Severity.LOW)
        ]
    }

    def 'SonarQubeCredential generates string for Basic authorization'() {
        expect:
        new SonarQubeCredential(token, username, password).value == 'Basic ' + expect

        where:
        token | username | password || expect
        'a'   | ''       | ''       || Base64.encoder.encodeToString('a:'.bytes)
        ''    | 'b'      | 'c'      || Base64.encoder.encodeToString('b:c'.bytes)
        'a'   | 'b'      | 'c'      || Base64.encoder.encodeToString('a:'.bytes)
    }

    def 'SonarQubeCredential throws error when valid params are not given'() {
        when:
        new SonarQubeCredential(token, username, password)

        then:
        final def e = thrown(RuntimeException)
        e.message == 'Please use SonarQubeCredential after setting valid items'

        where:
        token | username | password
        ''    | ''       | ''
        ''    | 'b'      | ''
        ''    | ''       | 'c'
    }

    def cleanup() {
        server.shutdown()
    }
}
