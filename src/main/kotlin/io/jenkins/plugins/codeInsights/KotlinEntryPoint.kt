package io.jenkins.plugins.codeInsights

import hudson.FilePath
import hudson.Launcher
import hudson.model.Run
import hudson.model.TaskListener
import io.jenkins.plugins.codeInsights.annotation.AnnotationsProviders

class KotlinEntryPoint(
    private val run: Run<*, *>,
    private val workspace: FilePath,
    private val launcher: Launcher,
    private val listener: TaskListener,
    private val bitbucketUrl: String,
    private val project: String,
    private val reportKey: String,
    private val username: String,
    private val password: String,
    private val repositoryName: String,
    private val srcPath: String,
    private val commitId: String,
    private val checkstyleFilePath: String?,
) {
    fun delegate() {
        JenkinsLogger.setLogger(listener.logger)
        val httpClient = HttpClient(
            username,
            password,
            bitbucketUrl,
            project,
            repositoryName,
            commitId,
            reportKey,
        )

        httpClient.putReport()

        AnnotationsProviders.Builder(workspace)
            .setCheckstyle(checkstyleFilePath)
            .build()
            .executeAndPost(httpClient)
    }
}
