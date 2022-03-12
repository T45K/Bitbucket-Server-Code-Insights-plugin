package io.jenkins.plugins.codeInsights

import io.jenkins.plugins.codeInsights.annotation.AnnotationsProviders
import java.io.PrintStream

class KotlinEntryPoint(
    private val bitbucketUrl: String,
    private val project: String,
    private val reportKey: String,
    private val username: String,
    private val password: String,
    private val repositoryName: String,
    private val repositoryPath: String,
    private val srcPath: String,
    private val commitId: String,
    private val checkstyleFilePath: String?,
    private val logger: PrintStream
) {
    fun delegate() {
        val httpClient = HttpClient(
            username,
            password,
            bitbucketUrl,
            project,
            repositoryName,
            commitId,
            reportKey,
            logger,
        )

        httpClient.putReport()

        AnnotationsProviders.Builder(repositoryPath, reportKey)
            .setCheckstyle(checkstyleFilePath)
            .build()
            .executeAndPost(httpClient)
    }
}
