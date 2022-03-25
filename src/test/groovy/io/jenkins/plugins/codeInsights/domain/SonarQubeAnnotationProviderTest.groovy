package io.jenkins.plugins.codeInsights.domain

import io.jenkins.plugins.codeInsights.testUtil.FileUtil
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import spock.lang.Specification

import java.nio.file.Paths

@SuppressWarnings('GroovyAccessibility')
class SonarQubeAnnotationProviderTest extends Specification {
    private static final def SONAR_QUBE_RESPONSE = FileUtil.readString(Paths.get('src', 'test', 'resources', 'sonarQubeResponse.json'))

    private final def server = new MockWebServer()

    def 'convert returns annotations'() {
        given:
        server.enqueue(new MockResponse().setBody(SONAR_QUBE_RESPONSE)) // call to fetch total page
        server.enqueue(new MockResponse().setBody(SONAR_QUBE_RESPONSE)) // call to fetch issue

        final def sut = new SonarQubeAnnotationProvider(server.url('').toString(), 'trial', new SonarQubeCredential('', 'admin', 'admin'))

        expect:
        sut.convert() == [
            new Annotation(1, 'SonarQube says: Rename this package name to match the regular expression \'^[a-z_]+(\\.[a-z_][a-z0-9_]*)*$\'.',
                'src/main/java/io/jenkins/plugins/codeInsights/CodeInsightsBuilder.java', Severity.LOW,
                server.url('').resolve("/project/issues?id=trial&issues=AX-YFnV_aoaIWGn8Z8OF").toString())
        ]
    }

    def 'convert returns emptyList when fetchTotalPage failed'() {
        given:
        server.enqueue(new MockResponse().setResponseCode(400))

        final def sut = new SonarQubeAnnotationProvider(server.url('').toString(), 'trial', new SonarQubeCredential('', 'admin', 'admin'))

        expect:
        sut.convert() == []
    }

    def 'convert returns emptyList when fetchTotalPage returns 0'() {
        given:
        server.enqueue(new MockResponse().setBody('{"paging": {"total": 0}}'))

        final def sut = new SonarQubeAnnotationProvider(server.url('').toString(), 'trial', new SonarQubeCredential('', 'admin', 'admin'))

        expect:
        sut.convert() == []
    }

    def 'convert returns emptyList when callApi failed'() {
        given:
        server.enqueue(new MockResponse().setBody(SONAR_QUBE_RESPONSE))
        server.enqueue(new MockResponse().setResponseCode(400))

        final def sut = new SonarQubeAnnotationProvider(server.url('').toString(), 'trial', new SonarQubeCredential('', 'admin', 'admin'))

        expect:
        sut.convert() == []
    }

    def 'convert returns emptyList when issues are missing'() {
        given:
        server.enqueue(new MockResponse().setBody(SONAR_QUBE_RESPONSE))
        server.enqueue(new MockResponse())

        final def sut = new SonarQubeAnnotationProvider(server.url('').toString(), 'trial', new SonarQubeCredential('', 'admin', 'admin'))

        expect:
        sut.convert() == []
    }

    def 'convert can handle missed some values'() {
        given:
        server.enqueue(new MockResponse().setBody(SONAR_QUBE_RESPONSE)) // call to fetch total page
        server.enqueue(new MockResponse().setBody('''\
{
  "issues": [
    {
      "component": "trial:src/main/java/io/jenkins/plugins/codeInsights/CodeInsightsBuilder.java",
      "message": "Rename this package name to match the regular expression \\u0027^[a-z_]+(\\\\.[a-z_][a-z0-9_]*)*$\\u0027."
    }
  ]
}
''')) // call to fetch issue

        final def sut = new SonarQubeAnnotationProvider(server.url('').toString(), 'trial', new SonarQubeCredential('', 'admin', 'admin'))

        expect:
        sut.convert() == [
            new Annotation(0, 'SonarQube says: Rename this package name to match the regular expression \'^[a-z_]+(\\.[a-z_][a-z0-9_]*)*$\'.',
                'src/main/java/io/jenkins/plugins/codeInsights/CodeInsightsBuilder.java', Severity.LOW, null)
        ]
    }

    def 'fetchTotalPage returns total page or null'() {
        given:
        server.enqueue(response)

        final def sut = new SonarQubeAnnotationProvider(server.url('').toString(), 'trial', new SonarQubeCredential('', 'admin', 'admin'))

        expect:
        sut.fetchTotalPage() == result

        where:
        response                                                     || result
        new MockResponse().setBody(SONAR_QUBE_RESPONSE)              || 1
        new MockResponse().setResponseCode(400).setBody('Not found') || null
    }

    def 'callApi can treat successful and failure response'() {
        given:
        server.enqueue(response)

        final def sut = new SonarQubeAnnotationProvider(server.url('').toString(), 'trial', new SonarQubeCredential('', 'admin', 'admin'))

        expect:
        sut.callApi(1) == result

        where:
        response                                                     || result
        new MockResponse().setBody(SONAR_QUBE_RESPONSE)              || SONAR_QUBE_RESPONSE
        new MockResponse().setResponseCode(400).setBody('Not found') || null
    }

    def 'toAnnotationSeverity converts from SonarQube string to enum'() {
        final def sut = new SonarQubeAnnotationProvider('', '', new SonarQubeCredential('a', '', ''))

        expect:
        sut.toAnnotationSeverity(input) == output

        where:
        input      || output
        'INFO'     || Severity.LOW
        'info'     || Severity.LOW
        'MINOR'    || Severity.LOW
        'minor'    || Severity.LOW
        'MAJOR'    || Severity.MEDIUM
        'major'    || Severity.MEDIUM
        'CRITICAL' || Severity.HIGH
        'critical' || Severity.HIGH
        'BLOCKERS' || Severity.HIGH
        'blockers' || Severity.HIGH
        'UNKNOWN'  || Severity.LOW
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
