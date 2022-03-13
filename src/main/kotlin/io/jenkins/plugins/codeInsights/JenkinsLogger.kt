package io.jenkins.plugins.codeInsights

import java.io.PrintStream

object JenkinsLogger {
	private var logger: PrintStream = System.out

	fun setLogger(logger: PrintStream) {
		this.logger = logger
	}

	fun info(message: String) {
		logger.println("[Code Insights plugin] $message")
	}
}
