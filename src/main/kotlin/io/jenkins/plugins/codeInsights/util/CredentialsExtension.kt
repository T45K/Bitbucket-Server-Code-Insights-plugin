package io.jenkins.plugins.codeInsights.util

import okhttp3.Credentials

@Suppress("UnusedReceiverParameter")
fun Credentials.bearer(secret: String): String = "Bearer $secret"
