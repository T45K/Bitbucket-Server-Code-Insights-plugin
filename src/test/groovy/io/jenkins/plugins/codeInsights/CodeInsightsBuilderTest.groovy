package io.jenkins.plugins.codeInsights

import net.sf.json.JSONObject
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Rule
import org.jvnet.hudson.test.JenkinsRule
import org.kohsuke.stapler.StaplerRequest
import spock.lang.Specification

import java.nio.file.Paths

class CodeInsightsBuilderTest extends Specification {

    @Rule
    final JenkinsRule jenkins = new JenkinsRule()

    final def server = new MockWebServer()

    final def jsonObject = new JSONObject(
        [codeInsights: [
            bitbucketUrl: server.url('').toString(),
            project     : 'project',
            reportKey   : 'reportKey',
            username    : 'username',
            password    : 'password'
        ]])

    final def INITIAL_COMMIT_ID = 'ed5582899d97af2ec47dad0462a7a38a8f3ebeeb'

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

    def 'test build without Checkstyle file'() {
        given:
        server.enqueue(new MockResponse().setResponseCode(200))

        def project = jenkins.createFreeStyleProject()
        def builder = new CodeInsightsBuilder('repo', 'src/main/java', INITIAL_COMMIT_ID)
        project.buildersList << builder
        project.customWorkspace = Paths.get(".").toAbsolutePath().toString()
        jenkins.get(CodeInsightsBuilder.DescriptorImpl).configure(Stub(StaplerRequest), jsonObject)

        expect:
        def build = jenkins.buildAndAssertSuccess(project)
        jenkins.assertLogContains('[Code Insights plugin] Start to put reports', build)
        jenkins.assertLogContains('[Code Insights plugin] Put reports: SUCCESS', build)
        jenkins.assertLogContains('Finished: SUCCESS', build)
        jenkins.assertLogNotContains('[Code Insights plugin] Checkstyle enabled', build)
    }

    def cleanup() {
        server.shutdown()
    }
}
