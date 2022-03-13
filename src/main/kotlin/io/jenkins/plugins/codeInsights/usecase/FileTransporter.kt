package io.jenkins.plugins.codeInsights.usecase

interface FileTransporter {
    fun copyFromWorkspaceToLocal(path: String)
}
