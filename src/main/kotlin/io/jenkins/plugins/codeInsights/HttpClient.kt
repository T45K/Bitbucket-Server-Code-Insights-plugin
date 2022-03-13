package io.jenkins.plugins.codeInsights

import io.jenkins.plugins.codeInsights.annotation.Annotation
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class HttpClient(
    username: String,
    password: String,
    bitbucketUrl: String,
    project: String,
    repository: String,
    commitId: String,
    reportKey: String,
) {
    private val client: OkHttpClient = OkHttpClient.Builder()
        .authenticator { _, response ->
            val credential = Credentials.basic(username, password)
            response.request.newBuilder()
                .header("Authorization", credential)
                .build()
        }.build()

    private val reportUrl =
        "$bitbucketUrl/rest/insights/1.0/projects/$project/repos/$repository/commits/$commitId/reports/$reportKey"
    private val annotationUrl =
        "$bitbucketUrl/rest/insights/1.0/projects/$project/repos/$repository/commits/$commitId/reports/$reportKey/annotations"

    private val credential = Credentials.basic(username, password)
    private val mediaType = "application/json; charset=utf-8".toMediaType()
    private val reportRequestBody = Json.encodeToString(
        mapOf(
            "title" to "Jenkins report",
            "reporter" to "Jenkins",
            "logoUrl" to "https://www.jenkins.io/images/logos/superhero/256.png",
        )
    ).toRequestBody(mediaType)

    fun putReport() {
        val request = Request.Builder()
            .url(reportUrl)
            .authorizationHeader()
            .put(reportRequestBody)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                JenkinsLogger.info("Put reports: SUCCESS")
            }

            override fun onFailure(call: Call, e: IOException) {
                JenkinsLogger.info("Put reports: FAILURE")
            }
        })
    }

    fun postAnnotations(name: String, annotations: List<Annotation>) {
        val request = Request.Builder()
            .url(annotationUrl)
            .authorizationHeader()
            .post(Json.encodeToString(mapOf("annotations" to annotations)).toRequestBody(mediaType))
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                JenkinsLogger.info("Post $name annotations: SUCCESS")
            }

            override fun onFailure(call: Call, e: IOException) {
                JenkinsLogger.info("Post $name annotations: FAILURE")
            }
        })
    }

    private fun Request.Builder.authorizationHeader(): Request.Builder = this.header("Authorization", credential)
}
