package io.jenkins.plugins.codeInsights.framework

import hudson.FilePath
import hudson.model.Run
import io.jenkins.plugins.codeInsights.usecase.FileTransporter

class FileTransporterImpl(private val workspace: FilePath, run: Run<*, *>) : FileTransporter {
    private val local = FilePath(run.rootDir)

    override fun copyFromWorkspaceToLocal(path: String) {
        workspace.child(path).copyRecursiveTo(local.child(path))
    }
}
