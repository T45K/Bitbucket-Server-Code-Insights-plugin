package io.jenkins.plugins.codeInsights

import io.jenkins.plugins.codeInsights.annotation.Annotation
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.Credentials
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import kotlin.math.min

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
	private val reportRequestBody = Json { encodeDefaults = true }.encodeToString(mapOf(
			"title" to "Jenkins report",
			"reporter" to "Jenkins",
			"logoUrl" to "https://www.jenkins.io/images/logos/superhero/256.png",
	)).toRequestBody(mediaType)

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
				JenkinsLogger.info(it.body!!.string())
				throw CallApiFailureException()
			}
		}
	}

	fun postAnnotations(name: String, annotations: List<Annotation>) {
		val repeatCount = (annotations.size + 999) / 1000
		repeat(repeatCount) { i ->
			JenkinsLogger.info("Start to post $name annotations (${i + 1}/$repeatCount)")
			val request = Request.Builder()
					.url(annotationUrl)
					.authorizationHeader()
					.post(Json { encodeDefaults = true }
							.encodeToString(mapOf("annotations" to annotations.subList(i * 1000, min((i + 1) * 1000, annotations.size))))
							.toRequestBody(mediaType))
					.build()
			client.newCall(request).execute().also {
				if (it.isSuccessful) {
					JenkinsLogger.info("Post $name annotations (${i + 1}/$repeatCount): SUCCESS")
				} else {
					JenkinsLogger.info(it.body!!.string())
					throw CallApiFailureException()
				}
			}
		}
	}

	private fun Request.Builder.authorizationHeader(): Request.Builder = this.header("Authorization", credential)

	private class CallApiFailureException : RuntimeException()
}
