package io.jenkins.plugins.codeInsights.test_util

import java.nio.file.Files

class FileUtil {
    static def createDirIfNotExist(dir) {
        if (Files.notExists(dir)) {
            Files.createDirectories(dir)
        } else {
            dir
        }
    }

    static def deleteRecursive(path) {
        if (Files.isDirectory(path)) {
            Files.list(path).forEach { deleteRecursive(it) }
        }
        Files.delete(path)
    }
}
