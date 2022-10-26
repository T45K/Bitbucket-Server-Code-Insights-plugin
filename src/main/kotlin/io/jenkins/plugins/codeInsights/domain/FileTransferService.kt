package io.jenkins.plugins.codeInsights.domain

interface FileTransferService {
    fun copyFromWorkspaceToLocal(path: String)
    fun readFile(path: String): String
}
