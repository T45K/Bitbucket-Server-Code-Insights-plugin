package io.jenkins.plugins.codeInsights.usecase

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import io.jenkins.plugins.codeInsights.domain.CheckstyleAnnotationProvider
import io.jenkins.plugins.codeInsights.domain.SonarQubeAnnotationProvider
import io.jenkins.plugins.codeInsights.domain.SonarQubeCredential
import io.jenkins.plugins.codeInsights.domain.SpotBugsAnnotationProvider
import spock.lang.Specification

class ExecutableAnnotationProvidersBuilderTest extends Specification {

    final def fileTransferServiceStub = Stub(FileTransferService) { readFile(_ as String) >> 'content' }

    def 'setCheckstyle can perform correctly'() {
        given:
        final def sut = new ExecutableAnnotationProvidersBuilder(fileTransferServiceStub)

        expect:
        sut.setCheckstyle(checkstyleFilePath, 'repo').build() == expect

        where:
        checkstyleFilePath || expect
        'result.xml'       || [new CheckstyleAnnotationProvider(new XmlMapper(), 'repo', 'content')]
        ''                 || []
    }

    def 'setSpotBugs can perform correctly'() {
        given:
        final def sut = new ExecutableAnnotationProvidersBuilder(fileTransferServiceStub)

        expect:
        sut.setSpotBugs(spotBugsFilePath, 'src').build() == expect

        where:
        spotBugsFilePath || expect
        'result.xml'     || [new SpotBugsAnnotationProvider('src', new XmlMapper(), 'content')]
        ''               || []
    }

//    def 'setSonarQube can perform correctly'() {
//        given:
//        final def sut = new ExecutableAnnotationProvidersBuilder(fileTransferServiceStub)
//
//        expect:
//        sut.setSonarQube(url, projectKey, token, username, password).build() == expect
//
//        where:
//        url   | projectKey   | token   | username   | password   || expect
//        'url' | 'projectKey' | 'token' | 'username' | 'password' || [new SonarQubeAnnotationProvider(url, projectKey, new SonarQubeCredential(token, username, password))]
//        'url' | 'projectKey' | 'token' | ''         | ''         || [new SonarQubeAnnotationProvider(url, projectKey, new SonarQubeCredential(token, username, password))]
//        'url' | 'projectKey' | ''      | 'username' | 'password' || [new SonarQubeAnnotationProvider(url, projectKey, new SonarQubeCredential(token, username, password))]
//        ''    | 'projectKey' | 'token' | 'username' | 'password' || []
//        'url' | ''           | 'token' | 'username' | 'password' || []
//        'url' | 'projectKey' | ''      | ''         | ''         || []
//    } TODO
}
