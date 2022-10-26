package io.jenkins.plugins.codeInsights.infrastructure

import io.jenkins.plugins.codeInsights.JenkinsLogger
import io.jenkins.plugins.codeInsights.domain.Annotation
import io.jenkins.plugins.codeInsights.domain.coverage.CoverageRequest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.Credentials
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class HttpClient(
    username: String,
    password: String,
    bitbucketUrl: String,
    project: String,
    repository: String,
    commitId: String,
    reportKey: String,
) {
    private val client: OkHttpClient = OkHttpClient()

    private val reportUrl = bitbucketUrl.toHttpUrl().newBuilder()
        .addPathSegment("rest")
        .addPathSegment("insights")
        .addPathSegment("1.0")
        .addPathSegment("projects")
        .addPathSegment(project)
        .addPathSegment("repos")
        .addPathSegment(repository)
        .addPathSegment("commits")
        .addPathSegment(commitId)
        .addPathSegment("reports")
        .addPathSegment(reportKey)
        .build()

    private val annotationUrl = reportUrl.newBuilder()
        .addPathSegment("annotations")
        .build()

    private val coverageUrl = bitbucketUrl.toHttpUrl().newBuilder()
        .addPathSegment("rest")
        .addPathSegment("code-coverage")
        .addPathSegment("1.0")
        .addPathSegment("commits")
        .addPathSegment(commitId)
        .build()

    private val credential = Credentials.basic(username, password)
    private val applicationJsonMediaType = "application/json; charset=utf-8".toMediaType()
    private val json = Json { encodeDefaults = true }

    private val reportRequestBody = json.encodeToString(
        mapOf(
            "title" to "Jenkins report",
            "reporter" to "Jenkins",
            "logoUrl" to "https://www.jenkins.io/images/logos/superhero/256.png",
        )
    ).toRequestBody(applicationJsonMediaType)

    fun putReport() {
        JenkinsLogger.info("Start to put reports")
        val request = Request.Builder()
            .url(reportUrl)
            .authorizationHeader()
            .put(reportRequestBody)
            .build()
        client.newCall(request).execute().also {
            if (it.isSuccessful) {
                JenkinsLogger.info("Put reports: SUCCESS")
            } else {
                JenkinsLogger.info("Put reports: FAILURE")
                JenkinsLogger.info(it.body!!.string())
                throw CallApiFailureException()
            }
        }
    }

    fun postAnnotations(name: String, annotations: List<Annotation>) {
        JenkinsLogger.info("Start to post $name annotations")
        val request = Request.Builder()
            .url(annotationUrl)
            .authorizationHeader()
            .post(json.encodeToString(mapOf("annotations" to annotations)).toRequestBody(applicationJsonMediaType))
            .build()
        client.newCall(request).execute().also {
            if (it.isSuccessful) {
                JenkinsLogger.info("Post $name annotations: SUCCESS")
            } else {
                JenkinsLogger.info("Post $name annotations: FAILURE")
                JenkinsLogger.info(it.body!!.string())
                throw CallApiFailureException()
            }
        }
    }

    fun postCoverage(coverageRequest: CoverageRequest) {
        JenkinsLogger.info("Start to post coverage")
        val request = Request.Builder()
            .url(coverageUrl)
            .authorizationHeader()
            .header("X-Atlassian-Token", "no-check")
            .post(json.encodeToString(coverageRequest).toRequestBody(applicationJsonMediaType))
            .build()
        client.newCall(request).execute().also {
            if (it.isSuccessful) {
                JenkinsLogger.info("Post coverage: SUCCESS")
            } else {
                JenkinsLogger.info("Post coverage: FAILURE")
                JenkinsLogger.info(it.body!!.string())
                throw CallApiFailureException()
            }
        }
    }

    private fun Request.Builder.authorizationHeader(): Request.Builder = this.header("Authorization", credential)

    private class CallApiFailureException : RuntimeException()
}
