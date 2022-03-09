package io.jenkins.plugins.codeInsights

import okhttp3.Call
import okhttp3.Callback
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.io.PrintStream

class HttpClient(
    username: String,
    password: String,
    bitbucketUrl: String,
    project: String,
    repository: String,
    commitId: String,
    reportKey: String,
    logger: PrintStream
) : JenkinsLoggable(logger) {
    private val client: OkHttpClient = OkHttpClient.Builder()
        .authenticator { _, response ->
            val credential = Credentials.basic(username, password)
            response.request.newBuilder().header("Authorization", credential).build()
        }.build()

    private val reportUrl =
        "$bitbucketUrl/rest/insights/1.0/projects/$project/repos/$repository/commits/$commitId/reports/$reportKey"
    private val annotationUrl =
        "$bitbucketUrl/rest/insights/1.0/projects/$project/repos/$repository/commits/$commitId/reports/$reportKey/annotations"

    fun putReport() {
        val request = Request.Builder()
            .url(reportUrl)
            .put("aa".toRequestBody())
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                logger.println("Put reports: SUCCESS")
            }

            override fun onFailure(call: Call, e: IOException) {
                logger.println("Put reports: FAILURE")
            }
        })
    }

    fun postAnnotations() {

    }
}
