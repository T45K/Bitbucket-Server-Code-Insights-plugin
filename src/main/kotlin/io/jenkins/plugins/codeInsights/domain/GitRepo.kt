package io.jenkins.plugins.codeInsights.domain

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.diff.DiffEntry.ChangeType.ADD
import org.eclipse.jgit.diff.DiffEntry.ChangeType.MODIFY
import org.eclipse.jgit.diff.DiffFormatter
import org.eclipse.jgit.diff.RawTextComparator
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.revwalk.filter.RevFilter
import org.eclipse.jgit.treewalk.AbstractTreeIterator
import org.eclipse.jgit.treewalk.CanonicalTreeParser
import org.eclipse.jgit.util.io.DisabledOutputStream
import java.io.File

/**
 * .git
 */
class GitRepo(fullPath: String) {
    private val git = File(fullPath)
        .let(::FileRepository)
        .let(::Git)

    fun detectChangedFiles(head: String, base: String): List<String> {
        val headObjectId: ObjectId = git.repository.resolve(head)
        val headTreeParser = prepareTreeParser(headObjectId)

        val baseObjectId: ObjectId = git.repository.resolve(base)
        val mergeBaseObjectId = findMergeBaseObject(headObjectId, baseObjectId)
        val baseTreeParser = prepareTreeParser(mergeBaseObjectId)

        return DiffFormatter(DisabledOutputStream.INSTANCE)
            .apply {
                setRepository(git.repository)
                setDiffComparator(RawTextComparator.DEFAULT)
                isDetectRenames = true
            }.scan(baseTreeParser, headTreeParser)
            .filter { it.changeType == ADD || it.changeType == MODIFY }
            .map { it.newPath }
    }

    private fun findMergeBaseObject(commit1: ObjectId, commit2: ObjectId): ObjectId =
        RevWalk(git.repository)
            .apply {
                revFilter = RevFilter.MERGE_BASE
                markStart(this.parseCommit(commit1))
                markStart(this.parseCommit(commit2))
            }.next().id

    private fun prepareTreeParser(objectId: ObjectId): AbstractTreeIterator {
        val revWalk = RevWalk(git.repository).apply { revFilter = RevFilter.MERGE_BASE }
        val commit = revWalk.parseCommit(objectId)
        val tree = revWalk.parseTree(commit.tree.id)
        val treeParser = CanonicalTreeParser().apply { reset(git.repository.newObjectReader(), tree) }
        revWalk.dispose()
        return treeParser
    }
}
