package io.jenkins.plugins.codeInsights.framework

import hudson.FilePath
import hudson.model.Run
import io.jenkins.plugins.codeInsights.testUtil.FileUtil
import spock.lang.IgnoreIf
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Paths

@IgnoreIf({ jvm.javaVersion.startsWith('17') })
class FileTransferServiceImplTest extends Specification {
    def 'copyFromWorkspaceToLocal copies target file'() {
        given:
        def createdDir = FileUtil.createDirIfNotExist(Paths.get('target', this.class.simpleName))
        final def runStub = Stub(Run) { rootDir >> createdDir.toFile() }
        final def workspace = new FilePath(Paths.get('.').toFile())
        final def testDirPath = 'src/test/resources'

        final def sut = new FileTransferServiceImpl(workspace, runStub)

        when:
        sut.copyFromWorkspaceToLocal(testDirPath)

        then:
        Files.exists(createdDir.resolve(testDirPath))

        cleanup:
        FileUtil.deleteRecursive(createdDir)
    }

    def 'readFile returns of remote file contents'() {
        given:
        final def runStub = Stub(Run)
        final def workspace = new FilePath(Paths.get('src', 'test', 'resources').toFile())
        final def sut = new FileTransferServiceImpl(workspace, runStub)

        expect:
        sut.readFile('gitrepo-modified') == '''\
before modified
'''
    }
}
