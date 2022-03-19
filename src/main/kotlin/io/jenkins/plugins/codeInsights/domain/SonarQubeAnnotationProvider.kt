package io.jenkins.plugins.codeInsights.domain

import io.jenkins.plugins.codeInsights.JenkinsLogger
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.Credentials
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request

private const val PAGE_SIZE = 500

/**
 * When fail to call API, this class returns empty list
 */
class SonarQubeAnnotationProvider(
    sonarQubeUrl: String,
    private val sonarQubeProjectKey: String,
    private val sonarQubeCredential: SonarQubeCredential,
) : AnnotationProvider(sonarQubeUrl) {
    override val name: String = "SonarQube"

    private val client = OkHttpClient()

    override fun convert(repositoryPath: String): List<Annotation> {
        val totalPage = fetchTotalPage() ?: return emptyList()
        return (1..(totalPage + PAGE_SIZE - 1) / PAGE_SIZE)
            .flatMap { page -> callApi(page)?.let { it.jsonObject["issues"]!!.jsonArray } ?: emptyList() }
            .map { it.jsonObject }
            .map { issue ->
                Annotation(
                    issue["line"]!!.jsonPrimitive.int,
                    "SonarQube says: " + issue["message"]!!.jsonPrimitive.content,
                    issue["component"]!!.jsonPrimitive.content.split(":").last(),
                    issue["severity"]!!.toString().toAnnotationSeverity()
                )
            }
    }

    private fun fetchTotalPage(): Int? =
        callApi(1)?.let {
            it.jsonObject["paging"]!!
                .jsonObject["total"]!!
                .jsonPrimitive.int
        }

    private fun callApi(page: Int): JsonElement? {
        val url = super.source.toHttpUrl().newBuilder()
            .addQueryParameter("componentKey", sonarQubeProjectKey)
            .addQueryParameter("p", page.toString())
            .addQueryParameter("ps", PAGE_SIZE.toString())
            .build()

        val request = Request.Builder()
            .addHeader("Authorization", sonarQubeCredential.value)
            .url(url)
            .get()
            .build()

        return client.newCall(request).execute().let {
            if (it.isSuccessful) {
                it.body!!.string().let(Json::parseToJsonElement)
            } else {
                JenkinsLogger.info("Failed to call $name API")
                JenkinsLogger.info(it.body!!.string())
                null
            }
        }
    }

    private fun String.toAnnotationSeverity(): Severity = when (this.uppercase()) {
        "INFO", "MINOR" -> Severity.LOW
        "MAJOR" -> Severity.MEDIUM
        "CRITICAL", "BLOCKERS" -> Severity.HIGH
        else -> Severity.LOW
    }
}

class SonarQubeCredential(
    token: String,
    username: String,
    password: String,
) {
    val value: String = when {
        token.isNotEmpty() -> Credentials.basic(token, "")
        username.isNotEmpty() && password.isNotEmpty() -> Credentials.basic(username, password)
        else -> throw RuntimeException("Please use SonarQubeCredential after setting valid items")
    }
}
