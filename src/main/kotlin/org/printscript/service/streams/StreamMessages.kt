package org.printscript.service.streams

import org.printscript.service.common.ErrorDetail
import org.printscript.service.lint.LintWarning
import java.time.Instant
import java.util.UUID

data class LintRequestMessage(
    val jobId: UUID? = null,
    val userId: String,
    val snippetId: UUID,
    val snippetVersion: Instant,
    val language: String,
    val version: String,
    val content: String,
    val rules: Map<String, Any?> = emptyMap(),
)

data class LintResultMessage(
    val jobId: UUID? = null,
    val snippetId: UUID,
    val snippetVersion: Instant,
    val warnings: List<LintWarning>,
    val errors: List<ErrorDetail>,
)

data class FormatRequestMessage(
    val jobId: UUID? = null,
    val userId: String,
    val snippetId: UUID,
    val snippetVersion: Instant,
    val language: String,
    val version: String,
    val content: String,
    val rules: Map<String, Any?> = emptyMap(),
)

data class FormatResultMessage(
    val jobId: UUID? = null,
    val snippetId: UUID,
    val snippetVersion: Instant,
    val formattedContent: String?,
    val errors: List<ErrorDetail>,
)

data class TestRequestMessage(
    val jobId: UUID? = null,
    val userId: String,
    val snippetId: UUID,
    val snippetVersion: Instant,
    val testId: UUID,
    val language: String,
    val version: String,
    val content: String,
    val inputs: List<String> = emptyList(),
    val expectedOutputs: List<String> = emptyList(),
)

data class TestResultMessage(
    val jobId: UUID? = null,
    val snippetId: UUID,
    val snippetVersion: Instant,
    val testId: UUID,
    val status: String,
    val outputs: List<String>,
    val error: ErrorDetail? = null,
)
