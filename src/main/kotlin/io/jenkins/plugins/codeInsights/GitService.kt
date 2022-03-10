package io.jenkins.plugins.codeInsights

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.eclipse.jgit.lib.Constants
import java.nio.file.Paths

class GitService {
    companion object {
        fun extractHeadCommitId(localRepositoryPath: String): String {
            val path = Paths.get(localRepositoryPath, ".git")
            val fileRepository = FileRepository(path.toString())
            return Git(fileRepository).repository
                .resolve(Constants.HEAD)
                .name
        }
    }
}
