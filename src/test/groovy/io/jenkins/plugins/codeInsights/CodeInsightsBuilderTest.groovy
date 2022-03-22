package io.jenkins.plugins.codeInsights

import hudson.model.Result
import io.jenkins.plugins.codeInsights.util.SonarQubeResponseHereDocument
import net.sf.json.JSONObject
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Rule
import org.jvnet.hudson.test.JenkinsRule
import org.kohsuke.stapler.StaplerRequest
import spock.lang.IgnoreIf
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Paths

class CodeInsightsBuilderTest extends Specification {
    private static final def INITIAL_COMMIT_ID = 'ed5582899d97af2ec47dad0462a7a38a8f3ebeeb'
    private static final def LOCAL_JENKINS_DIR = Paths.get('target', 'tmp')

    @Rule
    private final JenkinsRule jenkins = new JenkinsRule()

    private final def server = new MockWebServer()

    private final def jsonObject = new JSONObject(
        [codeInsights:
             [bitbucketUrl: server.url('').toString(),
              project     : 'project',
              reportKey   : 'reportKey',
              username    : 'username',
              password    : 'password']
        ])

    def setupSpec() {
        if (Files.notExists(LOCAL_JENKINS_DIR)) {
            Files.createDirectory(LOCAL_JENKINS_DIR)
        }
    }

    def 'test config round-trip'() {
        given:
        final def project = jenkins.createFreeStyleProject()
        project.buildersList << new CodeInsightsBuilder('repo', '0' * 40)
        final def roundTrippedProject = jenkins.configRoundtrip(project)

        expect:
        jenkins.assertEqualDataBoundBeans(
            new CodeInsightsBuilder('repo', '0' * 40),
            roundTrippedProject.buildersList[0]
        )
    }

    def 'build successful without specifying any source'() {
        given:
        server.enqueue(new MockResponse().setResponseCode(200))
        jenkins.get(CodeInsightsBuilder.DescriptorImpl).configure(Stub(StaplerRequest), jsonObject)

        final def project = jenkins.createFreeStyleProject()
        final def builder = new CodeInsightsBuilder('repo', INITIAL_COMMIT_ID)
        project.buildersList << builder
        project.customWorkspace = Paths.get('.').toAbsolutePath().toString()

        expect:
        final def build = jenkins.buildAndAssertSuccess(project)
        jenkins.assertLogContains('[Code Insights plugin] Start to put reports', build)
        jenkins.assertLogContains('[Code Insights plugin] Put reports: SUCCESS', build)
        jenkins.assertLogContains('Finished: SUCCESS', build)
        jenkins.assertLogNotContains('[Code Insights plugin] Checkstyle enabled', build)
        jenkins.assertLogNotContains('[Code Insights plugin] SonarQube enabled', build)
    }

    def 'build successful with Checkstyle file'() {
        given:
        server.enqueue(new MockResponse()) // put reports
        jenkins.get(CodeInsightsBuilder.DescriptorImpl).configure(Stub(StaplerRequest), jsonObject)

        final def project = jenkins.createFreeStyleProject()
        final def builder = new CodeInsightsBuilder('repo', INITIAL_COMMIT_ID)
        builder.setCheckstyleFilePath('src/test/resources/checkstyle-test.xml')
        project.buildersList << builder
        project.customWorkspace = Paths.get('.').toAbsolutePath().toString()

        expect:
        final def build = jenkins.buildAndAssertSuccess(project)
        jenkins.assertLogContains('[Code Insights plugin] Start to put reports', build)
        jenkins.assertLogContains('[Code Insights plugin] Put reports: SUCCESS', build)
        jenkins.assertLogContains('[Code Insights plugin] Checkstyle enabled', build)
        jenkins.assertLogContains('[Code Insights plugin] Finish Checkstyle', build)
        jenkins.assertLogContains('Finished: SUCCESS', build)
    }

    def 'build successful with SonarQube'() {
        given:
        server.enqueue(new MockResponse()) // put reports
        server.enqueue(new MockResponse().setBody(SonarQubeResponseHereDocument.RESPONSE_BODY)) // call to fetch total page
        server.enqueue(new MockResponse().setBody(SonarQubeResponseHereDocument.RESPONSE_BODY)) // call to fetch issues
        final def jsonObject = new JSONObject(
            [codeInsights:
                 [bitbucketUrl  : server.url('').toString(),
                  project       : 'project',
                  reportKey     : 'reportKey',
                  username      : 'username',
                  password      : 'password',
                  sonarQubeUrl  : server.url('').toString(),
                  sonarQubeToken: 'hoge']
            ])
        jenkins.get(CodeInsightsBuilder.DescriptorImpl).configure(Stub(StaplerRequest), jsonObject)

        final def project = jenkins.createFreeStyleProject()
        final def builder = new CodeInsightsBuilder('repo', INITIAL_COMMIT_ID)
        builder.setSonarQubeProjectKey("trial")
        project.buildersList << builder
        project.customWorkspace = Paths.get('.').toAbsolutePath().toString()

        expect:
        final def build = jenkins.buildAndAssertSuccess(project)
        jenkins.assertLogContains('[Code Insights plugin] Start to put reports', build)
        jenkins.assertLogContains('[Code Insights plugin] Put reports: SUCCESS', build)
        jenkins.assertLogContains('[Code Insights plugin] SonarQube enabled', build)
        jenkins.assertLogContains('[Code Insights plugin] Finish SonarQube', build)
        jenkins.assertLogContains('Finished: SUCCESS', build)
    }

    def 'build successful with all sources'() {
        given:
        server.enqueue(new MockResponse().setResponseCode(200))
        server.enqueue(new MockResponse().setBody(SonarQubeResponseHereDocument.RESPONSE_BODY)) // call to fetch total page
        server.enqueue(new MockResponse().setBody(SonarQubeResponseHereDocument.RESPONSE_BODY)) // call to fetch issues
        final def jsonObject = new JSONObject(
            [codeInsights:
                 [bitbucketUrl  : server.url('').toString(),
                  project       : 'project',
                  reportKey     : 'reportKey',
                  username      : 'username',
                  password      : 'password',
                  sonarQubeUrl  : server.url('').toString(),
                  sonarQubeToken: 'hoge']
            ])
        jenkins.get(CodeInsightsBuilder.DescriptorImpl).configure(Stub(StaplerRequest), jsonObject)

        final def project = jenkins.createFreeStyleProject()
        final def builder = new CodeInsightsBuilder('repo', INITIAL_COMMIT_ID)
        builder.setCheckstyleFilePath('src/test/resources/checkstyle-test.xml')
        builder.setSonarQubeProjectKey('trial')
        project.buildersList << builder
        project.customWorkspace = Paths.get('.').toAbsolutePath().toString()

        expect:
        final def build = jenkins.buildAndAssertSuccess(project)
        jenkins.assertLogContains('[Code Insights plugin] Start to put reports', build)
        jenkins.assertLogContains('[Code Insights plugin] Put reports: SUCCESS', build)
        jenkins.assertLogContains('[Code Insights plugin] Checkstyle enabled', build)
        jenkins.assertLogContains('[Code Insights plugin] SonarQube enabled', build)
        jenkins.assertLogContains('Finished: SUCCESS', build)
    }

    def 'build failure when plugin cannot get response from Bitbucket server'() {
        given:
        jenkins.get(CodeInsightsBuilder.DescriptorImpl).configure(Stub(StaplerRequest), jsonObject)

        final def project = jenkins.createFreeStyleProject()
        final def builder = new CodeInsightsBuilder('repo', INITIAL_COMMIT_ID)
        project.buildersList << builder
        project.customWorkspace = Paths.get('.').toAbsolutePath().toString()

        expect:
        final def build = jenkins.buildAndAssertStatus(Result.FAILURE, project)
        jenkins.assertLogContains('[Code Insights plugin] Start to put reports', build)
        jenkins.assertLogContains('Finished: FAILURE', build)
    }

    def 'build failure when plugin get error response from Bitbucket server'() {
        given:
        server.enqueue(new MockResponse().setResponseCode(500))
        jenkins.get(CodeInsightsBuilder.DescriptorImpl).configure(Stub(StaplerRequest), jsonObject)

        final def project = jenkins.createFreeStyleProject()
        final def builder = new CodeInsightsBuilder('repo', INITIAL_COMMIT_ID)
        project.buildersList << builder
        project.customWorkspace = Paths.get('.').toAbsolutePath().toString()

        expect:
        final def build = jenkins.buildAndAssertStatus(Result.FAILURE, project)
        jenkins.assertLogContains('[Code Insights plugin] Start to put reports', build)
        jenkins.assertLogContains('[Code Insights plugin] Put reports: FAILURE', build)
    }

    def cleanup() {
        server.shutdown()
    }
}
