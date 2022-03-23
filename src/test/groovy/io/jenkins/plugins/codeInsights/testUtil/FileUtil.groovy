package io.jenkins.plugins.codeInsights.testUtil


import java.nio.file.Files

class FileUtil {
    static def createDirIfNotExist(dir) {
        if (Files.notExists(dir)) {
            Files.createDirectories(dir)
        } else {
            dir
        }
    }

    static def readString(path) {
        Files.readAllLines(path).join('\n')
    }

    static def deleteRecursive(path) {
        if (Files.isDirectory(path)) {
            Files.list(path).forEach { deleteRecursive(it) }
        }
        Files.delete(path)
    }
}
