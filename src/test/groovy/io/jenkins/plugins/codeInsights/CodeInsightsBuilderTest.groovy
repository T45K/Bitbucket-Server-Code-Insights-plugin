package io.jenkins.plugins.codeInsights

import hudson.model.Result
import net.sf.json.JSONObject
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Rule
import org.jvnet.hudson.test.JenkinsRule
import org.kohsuke.stapler.StaplerRequest
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Paths

class CodeInsightsBuilderTest extends Specification {

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

    private static final def INITIAL_COMMIT_ID = 'ed5582899d97af2ec47dad0462a7a38a8f3ebeeb'

    private static final def LOCAL_JENKINS_DIR = Paths.get('target', 'tmp')

    def setupSpec() {
        if (Files.notExists(LOCAL_JENKINS_DIR)) {
            Files.createDirectory(LOCAL_JENKINS_DIR)
        }
    }

    def 'test config round-trip'() {
        given:
        def project = jenkins.createFreeStyleProject()
        project.buildersList << new CodeInsightsBuilder('repo', 'src/main/java', '0' * 40)
        project = jenkins.configRoundtrip(project)

        expect:
        jenkins.assertEqualDataBoundBeans(
            new CodeInsightsBuilder('repo', 'src/main/java', '0' * 40),
            project.buildersList[0]
        )
    }

    def 'test build successful without Checkstyle file'() {
        given:
        server.enqueue(new MockResponse().setResponseCode(200))
        jenkins.get(CodeInsightsBuilder.DescriptorImpl).configure(Stub(StaplerRequest), jsonObject)

        def project = jenkins.createFreeStyleProject()
        def builder = new CodeInsightsBuilder('repo', 'src/main/java', INITIAL_COMMIT_ID)
        project.buildersList << builder
        project.customWorkspace = Paths.get(".").toAbsolutePath().toString()

        expect:
        def build = jenkins.buildAndAssertSuccess(project)
        jenkins.assertLogContains('[Code Insights plugin] Start to put reports', build)
        jenkins.assertLogContains('[Code Insights plugin] Put reports: SUCCESS', build)
        jenkins.assertLogContains('Finished: SUCCESS', build)
        jenkins.assertLogNotContains('[Code Insights plugin] Checkstyle enabled', build)
    }

    def 'test build successful with Checkstyle file'() {
        given:
        server.enqueue(new MockResponse().setResponseCode(200))
        server.enqueue(new MockResponse().setResponseCode(200))
        jenkins.get(CodeInsightsBuilder.DescriptorImpl).configure(Stub(StaplerRequest), jsonObject)

        def project = jenkins.createFreeStyleProject()
        def builder = new CodeInsightsBuilder('repo', 'src/main/java', INITIAL_COMMIT_ID)
        builder.setCheckstyleFilePath('src/test/resources/checkstyle-test.xml')
        project.buildersList << builder
        project.customWorkspace = Paths.get(".").toAbsolutePath().toString()

        expect:
        def build = jenkins.buildAndAssertSuccess(project)
        jenkins.assertLogContains('[Code Insights plugin] Start to put reports', build)
        jenkins.assertLogContains('[Code Insights plugin] Put reports: SUCCESS', build)
        jenkins.assertLogContains('[Code Insights plugin] Checkstyle enabled', build)
        jenkins.assertLogContains('[Code Insights plugin] Start to post Checkstyle annotations', build)
        jenkins.assertLogContains('[Code Insights plugin] Post Checkstyle annotations: SUCCESS', build)
        jenkins.assertLogContains('[Code Insights plugin] Finish Checkstyle', build)
        jenkins.assertLogContains('Finished: SUCCESS', build)
    }

    def 'test build failure when plugin cannot get response from Bitbucket server'() {
        given:
        jenkins.get(CodeInsightsBuilder.DescriptorImpl).configure(Stub(StaplerRequest), jsonObject)

        def project = jenkins.createFreeStyleProject()
        def builder = new CodeInsightsBuilder('repo', 'src/main/java', INITIAL_COMMIT_ID)
        project.buildersList << builder
        project.customWorkspace = Paths.get(".").toAbsolutePath().toString()

        expect:
        def build = jenkins.buildAndAssertStatus(Result.FAILURE, project)
        jenkins.assertLogContains('[Code Insights plugin] Start to put reports', build)
        jenkins.assertLogContains('Finished: FAILURE', build)
    }

    def 'test build failure when plugin get error response from Bitbucket server'() {
        given:
        server.enqueue(new MockResponse().setResponseCode(500))
        jenkins.get(CodeInsightsBuilder.DescriptorImpl).configure(Stub(StaplerRequest), jsonObject)

        def project = jenkins.createFreeStyleProject()
        def builder = new CodeInsightsBuilder('repo', 'src/main/java', INITIAL_COMMIT_ID)
        project.buildersList << builder
        project.customWorkspace = Paths.get(".").toAbsolutePath().toString()

        expect:
        def build = jenkins.buildAndAssertStatus(Result.FAILURE, project)
        jenkins.assertLogContains('[Code Insights plugin] Start to put reports', build)
        jenkins.assertLogContains('[Code Insights plugin] Put reports: FAILURE', build)
    }

    def cleanup() {
        server.shutdown()
    }
}
