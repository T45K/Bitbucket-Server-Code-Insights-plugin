package io.jenkins.plugins.codeInsights.util

import java.nio.file.Path

fun Path.toForwardSlashString() = this.toString().replace("\\", "/")
