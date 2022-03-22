package io.jenkins.plugins.codeInsights.util

import com.fasterxml.jackson.databind.JsonNode

fun JsonNode.asArray(): Iterable<JsonNode> = if (this.isArray) this else listOf(this)
