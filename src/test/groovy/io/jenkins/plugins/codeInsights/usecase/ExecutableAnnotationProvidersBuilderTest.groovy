package io.jenkins.plugins.codeInsights.usecase

import io.jenkins.plugins.codeInsights.JenkinsLogger
import io.jenkins.plugins.codeInsights.domain.CheckstyleAnnotationProvider
import io.jenkins.plugins.codeInsights.domain.PmdAnnotationProvider
import io.jenkins.plugins.codeInsights.domain.SonarQubeAnnotationProvider
import io.jenkins.plugins.codeInsights.domain.SpotBugsAnnotationProvider
import spock.lang.Specification

class ExecutableAnnotationProvidersBuilderTest extends Specification {

    final def fileTransferServiceStub = Stub(FileTransferService) { readFile(_ as String) >> 'content' }

    final def mockLogger = Mock(PrintStream)

    def setup() {
        JenkinsLogger.INSTANCE.setLogger(mockLogger)
    }

    def 'setCheckstyle set annotation provider when checkstyle file path is given'() {
        given:
        final def sut = new ExecutableAnnotationProvidersBuilder(fileTransferServiceStub)

        when:
        def executables = sut.setCheckstyle('result.xml', repositoryPath).build()

        then:
        executables.size() == 1
        executables[0].class == CheckstyleAnnotationProvider
        1 * mockLogger.println('[Code Insights plugin] Checkstyle enabled')

        where:
        repositoryPath << ['repo', '.', '/']
    }

    def 'setCheckstyle does not set annotation provider when checkstyle file path is not given'() {
        given:
        final def sut = new ExecutableAnnotationProvidersBuilder(fileTransferServiceStub)

        when:
        def executables = sut.setCheckstyle('', repositoryPath).build()

        then:
        executables.isEmpty()
        0 * mockLogger.println('[Code Insights plugin] Checkstyle enabled')

        where:
        repositoryPath << ['repo', '.', '/']
    }

    def 'setSpotBugs set annotation provider when SpotBugs file path is given'() {
        given:
        final def sut = new ExecutableAnnotationProvidersBuilder(fileTransferServiceStub)

        when:
        def executables = sut.setSpotBugs('result.xml', srcPath).build()

        then:
        executables.size() == 1
        executables[0].class == SpotBugsAnnotationProvider
        1 * mockLogger.println('[Code Insights plugin] SpotBugs enabled')

        where:
        srcPath << ['src', '.', '/']
    }

    def 'setSpotBugs does not set annotation provider when SpotBugs file path is not given'() {
        given:
        final def sut = new ExecutableAnnotationProvidersBuilder(fileTransferServiceStub)

        when:
        def executables = sut.setSpotBugs('', srcPath).build()

        then:
        executables.isEmpty()
        0 * mockLogger.println('[Code Insights plugin] SpotBugs enabled')

        where:
        srcPath << ['src', '.', '/']
    }

    def 'setPmd set annotation provider when PMD file path is given'() {
        given:
        final def sut = new ExecutableAnnotationProvidersBuilder(fileTransferServiceStub)

        when:
        def executables = sut.setPmd('result.xml', repositoryPath).build()

        then:
        executables.size() == 1
        executables[0].class == PmdAnnotationProvider
        1 * mockLogger.println('[Code Insights plugin] PMD enabled')

        where:
        repositoryPath << ['repo', '.', '/']
    }

    def 'setPmd does not set annotation provider when PMD file path is not given'() {
        given:
        final def sut = new ExecutableAnnotationProvidersBuilder(fileTransferServiceStub)

        when:
        def executables = sut.setPmd('', repositoryPath).build()

        then:
        executables.isEmpty()
        0 * mockLogger.println('[Code Insights plugin] PMD enabled')

        where:
        repositoryPath << ['src', '.', '/']
    }

    def 'setSonarQube set annotation provider when SonarQube url, project key, and account information are given'() {
        given:
        final def sut = new ExecutableAnnotationProvidersBuilder(fileTransferServiceStub)

        when:
        def executables = sut.setSonarQube(url, projectKey, token, username, password).build()

        then:
        executables.size() == 1
        executables[0].class == SonarQubeAnnotationProvider
        1 * mockLogger.println('[Code Insights plugin] SonarQube enabled')

        where:
        url   | projectKey   | token   | username   | password
        'url' | 'projectKey' | 'token' | 'username' | 'password'
        'url' | 'projectKey' | 'token' | ''         | ''
        'url' | 'projectKey' | ''      | 'username' | 'password'
    }

    def 'setSonarQube does not set annotation provider when either SonarQube url or project key are not given'() {
        given:
        final def sut = new ExecutableAnnotationProvidersBuilder(fileTransferServiceStub)

        when:
        def executables = sut.setSonarQube(url, projectKey, 'token', '', 'password').build()

        then:
        executables.isEmpty()
        0 * mockLogger.println('[Code Insights plugin] SonarQube enabled')

        where:
        url   | projectKey
        ''    | 'projectKey'
        'url' | ''
        ''    | ''
    }

    def 'setSonarQube shows error log and does not set annotation provider when account information is not given'() {
        given:
        final def sut = new ExecutableAnnotationProvidersBuilder(fileTransferServiceStub)

        when:
        def executables = sut.setSonarQube('url', 'projectKey', token, username, password).build()

        then:
        executables.isEmpty()
        0 * mockLogger.println('[Code Insights plugin] SonarQube enabled')
        1 * mockLogger.println('[Code Insights plugin] SonarQube credential items are not given')

        where:
        token | username   | password
        ''    | 'username' | ''
        ''    | ''         | 'password'
        ''    | ''         | ''
    }

    def 'build returns all providers set by setXXX method'() {
        given:
        final def sut = new ExecutableAnnotationProvidersBuilder(fileTransferServiceStub)

        expect:
        sut.setCheckstyle('result.xml', 'repo')
            .setSpotBugs('result.xml', 'src')
            .setPmd('result.xml', 'repo')
            .setSonarQube('url', 'projectKey', 'token', '', '')
            .build()*.class ==~ [CheckstyleAnnotationProvider, SpotBugsAnnotationProvider, PmdAnnotationProvider, SonarQubeAnnotationProvider]
    }
}
