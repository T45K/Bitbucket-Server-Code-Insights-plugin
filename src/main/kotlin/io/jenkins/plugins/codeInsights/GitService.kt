package io.jenkins.plugins.codeInsights

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.eclipse.jgit.lib.Constants
import java.io.File
import java.nio.file.Paths

class GitService {
    companion object {
        fun extractHeadCommitId(localRepositoryPath: File): String {
            val fileRepository = FileRepository(localRepositoryPath.resolve(".git"))
            return Git(fileRepository).repository
                .resolve(Constants.HEAD)
                .name
        }
    }
}
