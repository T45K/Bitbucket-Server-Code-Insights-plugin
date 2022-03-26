package io.jenkins.plugins.codeInsights.util

fun String.flat() = this.replace("\n", "")
    .replace("\\s+".toRegex(), " ")
    .trim()
