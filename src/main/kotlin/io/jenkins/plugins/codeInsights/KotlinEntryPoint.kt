package io.jenkins.plugins.codeInsights

import hudson.FilePath
import hudson.Launcher
import hudson.model.Run
import hudson.model.TaskListener
import io.jenkins.plugins.codeInsights.usecase.ExecutableAnnotationProvidersBuilder
import io.jenkins.plugins.codeInsights.usecase.GitRepo
import io.jenkins.plugins.codeInsights.framework.FileTransferServiceImpl

@Suppress("unused")
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
    private val baseBranch: String,
    private val checkstyleFilePath: String,
) {
    init {
        JenkinsLogger.setLogger(listener.logger)
    }

    fun delegate() {
        val httpClient = HttpClient(
            username, password, // credential
            bitbucketUrl, project, repositoryName, commitId, reportKey, // url
        )

        httpClient.putReport()

        val fileTransferService = FileTransferServiceImpl(workspace, run)
        fileTransferService.copyFromWorkspaceToLocal(".git")
        val changedFiles = GitRepo(run.rootDir.resolve(".git").absolutePath)
            .detectChangedFiles(commitId, baseBranch)
        val executables = ExecutableAnnotationProvidersBuilder(fileTransferService)
            .setCheckstyle(checkstyleFilePath)
            .build()
        for (executable in executables) {
            JenkinsLogger.info("Start ${executable.name}")
            val annotations = executable.convert(workspace.remote).filter { changedFiles.contains(it.path) }
            if (annotations.isNotEmpty()) {
                httpClient.postAnnotations(executable.name, annotations)
            }
            JenkinsLogger.info("Finish ${executable.name}")
        }
    }
}
