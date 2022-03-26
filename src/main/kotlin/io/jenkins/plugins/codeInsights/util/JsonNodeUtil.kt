package io.jenkins.plugins.codeInsights.util

import com.fasterxml.jackson.databind.JsonNode

fun JsonNode?.asArray(): Iterable<JsonNode> = this?.let { if (this.isArray) this else listOf(this) } ?: emptyList()
