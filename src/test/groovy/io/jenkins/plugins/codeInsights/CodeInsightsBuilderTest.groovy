package io.jenkins.plugins.codeInsights

import com.cloudbees.plugins.credentials.CredentialsProvider
import com.cloudbees.plugins.credentials.CredentialsScope
import com.cloudbees.plugins.credentials.domains.Domain
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl
import hudson.model.Result
import io.jenkins.plugins.codeInsights.testUtil.FileUtil
import net.sf.json.JSONObject
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Rule
import org.jvnet.hudson.test.JenkinsRule
import org.kohsuke.stapler.StaplerRequest
import spock.lang.Specification

import java.nio.file.Paths

class CodeInsightsBuilderTest extends Specification {
    private static final def INITIAL_COMMIT_ID = 'ed5582899d97af2ec47dad0462a7a38a8f3ebeeb'
    private static final def LOCAL_JENKINS_DIR = Paths.get('target', 'tmp')
    private static final def SONAR_QUBE_RESPONSE = FileUtil.readString(Paths.get('src', 'test', 'resources', 'sonarQubeResponse.json'))

    @Rule
    private final JenkinsRule jenkins = new JenkinsRule()

    private final def server = new MockWebServer()

    private final def jsonObject = new JSONObject(
        [
            codeInsights: [
                bitbucketUrl         : server.url('').toString(),
                project              : 'project',
                reportKey            : 'reportKey',
                bitbucketCredentialId: 'username:password'
            ]
        ]
    )

    def setupSpec() {
        FileUtil.createDirIfNotExist(LOCAL_JENKINS_DIR)
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
        server.enqueue(new MockResponse()) // put reports
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
        jenkins.assertLogNotContains('[Code Insights plugin] SpotBugs enabled', build)
        jenkins.assertLogNotContains('[Code Insights plugin] SonarQube enabled', build)
    }

    def 'build successful with Checkstyle file'() {
        given:
        server.enqueue(new MockResponse()) // put reports
        jenkins.get(CodeInsightsBuilder.DescriptorImpl).configure(Stub(StaplerRequest), jsonObject)

        final def project = jenkins.createFreeStyleProject()
        final def builder = new CodeInsightsBuilder('repo', INITIAL_COMMIT_ID)
        builder.setCheckstyleFilePath('target/checkstyle-result.xml')
        project.buildersList << builder
        project.customWorkspace = Paths.get('.').toAbsolutePath().toString()

        expect:
        final def build = jenkins.buildAndAssertSuccess(project)
        jenkins.assertLogContains('[Code Insights plugin] Start to put reports', build)
        jenkins.assertLogContains('[Code Insights plugin] Put reports: SUCCESS', build)
        jenkins.assertLogContains('[Code Insights plugin] Checkstyle enabled', build)
        jenkins.assertLogContains('[Code Insights plugin] Start Checkstyle', build)
        jenkins.assertLogContains('[Code Insights plugin] Finish Checkstyle', build)
        jenkins.assertLogContains('Finished: SUCCESS', build)
    }

    def 'build successful with SpotBugs file'() {
        given:
        server.enqueue(new MockResponse()) // put reports
        jenkins.get(CodeInsightsBuilder.DescriptorImpl).configure(Stub(StaplerRequest), jsonObject)

        final def project = jenkins.createFreeStyleProject()
        final def builder = new CodeInsightsBuilder('repo', INITIAL_COMMIT_ID)
        builder.setSpotBugsFilePath('src/test/resources/spotbugs-test.xml')
        project.buildersList << builder
        project.customWorkspace = Paths.get('.').toAbsolutePath().toString()

        expect:
        final def build = jenkins.buildAndAssertSuccess(project)
        jenkins.assertLogContains('[Code Insights plugin] Start to put reports', build)
        jenkins.assertLogContains('[Code Insights plugin] Put reports: SUCCESS', build)
        jenkins.assertLogContains('[Code Insights plugin] SpotBugs enabled', build)
        jenkins.assertLogContains('[Code Insights plugin] Start SpotBugs', build)
        jenkins.assertLogContains('[Code Insights plugin] Finish SpotBugs', build)
        jenkins.assertLogContains('Finished: SUCCESS', build)
    }

    def 'build successful with PMD file'() {
        given:
        server.enqueue(new MockResponse()) // put reports
        jenkins.get(CodeInsightsBuilder.DescriptorImpl).configure(Stub(StaplerRequest), jsonObject)

        final def project = jenkins.createFreeStyleProject()
        final def builder = new CodeInsightsBuilder('repo', INITIAL_COMMIT_ID)
        builder.setPmdFilePath('target/pmd.xml')
        project.buildersList << builder
        project.customWorkspace = Paths.get('.').toAbsolutePath().toString()

        expect:
        final def build = jenkins.buildAndAssertSuccess(project)
        jenkins.assertLogContains('[Code Insights plugin] Start to put reports', build)
        jenkins.assertLogContains('[Code Insights plugin] Put reports: SUCCESS', build)
        jenkins.assertLogContains('[Code Insights plugin] PMD enabled', build)
        jenkins.assertLogContains('[Code Insights plugin] Start PMD', build)
        jenkins.assertLogContains('[Code Insights plugin] Finish PMD', build)
        jenkins.assertLogContains('Finished: SUCCESS', build)
    }

    def 'build successful with SonarQube'() {
        given:
        CredentialsProvider.saveAll()
        server.enqueue(new MockResponse()) // put reports
        server.enqueue(new MockResponse().setBody(SONAR_QUBE_RESPONSE)) // call to fetch total page
        server.enqueue(new MockResponse().setBody(SONAR_QUBE_RESPONSE)) // call to fetch issues
        final def jsonObject = new JSONObject(
            [
                codeInsights: [
                    bitbucketUrl         : server.url('').toString(),
                    project              : 'project',
                    reportKey            : 'reportKey',
                    bitbucketCredentialId: 'username:password',
                    sonarQubeUrl         : server.url('').toString(),
                    sonarQubeCredentialId: 'sonarqube'
                ]
            ]
        )
        jenkins.get(CodeInsightsBuilder.DescriptorImpl).configure(Stub(StaplerRequest), jsonObject)
        CredentialsProvider.lookupStores(jenkins)
            .iterator()
            .next()
            .addCredentials(
                Domain.global(),
                new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL, 'sonarqube', '', 'username', 'password')
            )

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
        jenkins.assertLogContains('[Code Insights plugin] Start SonarQube', build)
        jenkins.assertLogContains('[Code Insights plugin] Finish SonarQube', build)
        jenkins.assertLogContains('Finished: SUCCESS', build)
    }

    def 'build successful with all sources'() {
        given:
        server.enqueue(new MockResponse())
        server.enqueue(new MockResponse().setBody(SONAR_QUBE_RESPONSE)) // call to fetch total page
        server.enqueue(new MockResponse().setBody(SONAR_QUBE_RESPONSE)) // call to fetch issues
        final def jsonObject = new JSONObject(
            [
                codeInsights: [
                    bitbucketUrl         : server.url('').toString(),
                    project              : 'project',
                    reportKey            : 'reportKey',
                    bitbucketCredentialId: 'username:password',
                    sonarQubeUrl         : server.url('').toString(),
                    sonarQubeCredentialId: 'sonarqube'
                ]
            ]
        )
        jenkins.get(CodeInsightsBuilder.DescriptorImpl).configure(Stub(StaplerRequest), jsonObject)
        CredentialsProvider.lookupStores(jenkins)
            .iterator()
            .next()
            .addCredentials(
                Domain.global(),
                new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL, 'sonarqube', '', 'username', 'password')
            )

        final def project = jenkins.createFreeStyleProject()
        final def builder = new CodeInsightsBuilder('repo', INITIAL_COMMIT_ID)
        builder.setCheckstyleFilePath('target/checkstyle-result.xml')
        builder.setSpotBugsFilePath('src/test/resources/spotbugs-test.xml')
        builder.setPmdFilePath('target/pmd.xml')
        builder.setSonarQubeProjectKey('trial')
        project.buildersList << builder
        project.customWorkspace = Paths.get('.').toAbsolutePath().toString()

        expect:
        final def build = jenkins.buildAndAssertSuccess(project)
        jenkins.assertLogContains('[Code Insights plugin] Start to put reports', build)
        jenkins.assertLogContains('[Code Insights plugin] Put reports: SUCCESS', build)
        jenkins.assertLogContains('[Code Insights plugin] Checkstyle enabled', build)
        jenkins.assertLogContains('[Code Insights plugin] SpotBugs enabled', build)
        jenkins.assertLogContains('[Code Insights plugin] PMD enabled', build)
        jenkins.assertLogContains('[Code Insights plugin] SonarQube enabled', build)
        jenkins.assertLogContains('[Code Insights plugin] Start Checkstyle', build)
        jenkins.assertLogContains('[Code Insights plugin] Finish Checkstyle', build)
        jenkins.assertLogContains('[Code Insights plugin] Start SpotBugs', build)
        jenkins.assertLogContains('[Code Insights plugin] Finish SpotBugs', build)
        jenkins.assertLogContains('[Code Insights plugin] Start PMD', build)
        jenkins.assertLogContains('[Code Insights plugin] Finish PMD', build)
        jenkins.assertLogContains('[Code Insights plugin] Start SonarQube', build)
        jenkins.assertLogContains('[Code Insights plugin] Finish SonarQube', build)
        jenkins.assertLogContains('Finished: SUCCESS', build)
    }

    def 'build failure when plugin cannot find input file path'() {
        given:
        jenkins.get(CodeInsightsBuilder.DescriptorImpl).configure(Stub(StaplerRequest), jsonObject)

        final def project = jenkins.createFreeStyleProject()
        final def builder = new CodeInsightsBuilder('repo', INITIAL_COMMIT_ID)
        builder.setCheckstyleFilePath('nothing')
        project.buildersList << builder
        project.customWorkspace = Paths.get('.').toAbsolutePath().toString()

        expect:
        final def build = jenkins.buildAndAssertStatus(Result.FAILURE, project)
        jenkins.assertLogContains('[Code Insights plugin] Start to put reports', build)
        jenkins.assertLogContains('Finished: FAILURE', build)
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

    def 'DescriptorImpl can be saved and loaded'() {
        given:
        final def jsonObject = new JSONObject(
            [
                codeInsights: [
                    bitbucketUrl         : server.url('').toString(),
                    project              : 'project',
                    reportKey            : 'reportKey',
                    bitbucketCredentialId: 'username:password',
                    sonarQubeUrl         : server.url('').toString(),
                    sonarQubeCredentialId: 'sonarqube'
                ]
            ]
        )
        jenkins.get(CodeInsightsBuilder.DescriptorImpl).configure(Stub(StaplerRequest), jsonObject)

        when:
        final def descriptor = new CodeInsightsBuilder.DescriptorImpl()

        then:
        descriptor.bitbucketUrl == server.url('').toString()
        descriptor.project == 'project'
        descriptor.reportKey == 'reportKey'
        descriptor.bitbucketCredentialId == 'username:password'
        descriptor.sonarQubeUrl == server.url('').toString()
        descriptor.sonarQubeCredentialId == 'sonarqube'
    }

    def cleanup() {
        server.shutdown()
    }
}
