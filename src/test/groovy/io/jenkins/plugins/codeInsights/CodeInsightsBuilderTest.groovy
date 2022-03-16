package io.jenkins.plugins.codeInsights

import hudson.model.Result
import net.sf.json.JSONObject
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Rule
import org.jvnet.hudson.test.JenkinsRule
import org.kohsuke.stapler.StaplerRequest
import spock.lang.Specification

class CodeInsightsBuilderTest extends Specification {

    @Rule
    final JenkinsRule jenkins = new JenkinsRule()

    final def server = new MockWebServer()

    def jsonObject = new JSONObject(
        [codeInsights: [
            bitbucketUrl: server.url('').toString(),
            project     : 'project',
            reportKey   : 'reportKey',
            username    : 'username',
            password    : 'password'
        ]])

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
        def builder = new CodeInsightsBuilder('repo', 'src/main/java', '0' * 40)
        project.buildersList << builder
        project.customWorkspace = '.'
        jenkins.get(CodeInsightsBuilder.DescriptorImpl).configure(Stub(StaplerRequest), jsonObject)

        expect:
        def build = jenkins.buildAndAssertStatus(Result.FAILURE, project)
        jenkins.assertLogContains('[Code Insights plugin] Start to put reports', build)
        jenkins.assertLogNotContains('[Code Insights plugin] Checkstyle enabled', build)
    }

    def cleanup() {
        server.shutdown()
    }
}
