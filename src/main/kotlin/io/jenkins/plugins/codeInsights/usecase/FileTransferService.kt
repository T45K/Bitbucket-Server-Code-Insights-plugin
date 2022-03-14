package io.jenkins.plugins.codeInsights.usecase

interface FileTransferService {
    fun copyFromWorkspaceToLocal(path: String)
    fun readFile(path: String): String
}
