package org.printscript.service.streams

import org.printscript.service.execute.ExecuteRequest
import org.printscript.service.execute.ExecutionService
import org.printscript.service.format.FormatRequest
import org.printscript.service.format.FormattingService
import org.printscript.service.lint.LintRequest
import org.printscript.service.lint.LintingService
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.node.ObjectNode

@Component
class LintWorkerHandler(
    private val linting: LintingService,
    private val producer: StreamProducer,
    private val objectMapper: ObjectMapper,
) : StreamHandler {
    override val stream = StreamNames.LINT_REQUESTS
    override val group = StreamNames.PRINTSCRIPT_WORKERS
    override val dlqStream = StreamNames.LINT_REQUESTS_DLQ

    override fun handle(payload: String) {
        val msg = objectMapper.readValue(payload, LintRequestMessage::class.java)
        val rulesNode = msg.rules.toObjectNode(objectMapper)
        val res = linting.lint(LintRequest(msg.language, msg.version, msg.content, rulesNode))
        producer.publish(
            StreamNames.LINT_RESULTS,
            LintResultMessage(msg.jobId, msg.snippetId, msg.snippetVersion, res.warnings, res.errors),
        )
    }
}

@Component
class FormatWorkerHandler(
    private val formatting: FormattingService,
    private val producer: StreamProducer,
    private val objectMapper: ObjectMapper,
) : StreamHandler {
    override val stream = StreamNames.FORMAT_REQUESTS
    override val group = StreamNames.PRINTSCRIPT_WORKERS
    override val dlqStream = StreamNames.FORMAT_REQUESTS_DLQ

    override fun handle(payload: String) {
        val msg = objectMapper.readValue(payload, FormatRequestMessage::class.java)
        val rulesNode = msg.rules.toObjectNode(objectMapper)
        val res = formatting.format(FormatRequest(msg.language, msg.version, msg.content, rulesNode))
        producer.publish(
            StreamNames.FORMAT_RESULTS,
            FormatResultMessage(msg.jobId, msg.snippetId, msg.snippetVersion, res.content, res.errors),
        )
    }
}

@Component
class TestWorkerHandler(
    private val execution: ExecutionService,
    private val producer: StreamProducer,
    private val objectMapper: ObjectMapper,
) : StreamHandler {
    override val stream = StreamNames.TEST_REQUESTS
    override val group = StreamNames.PRINTSCRIPT_WORKERS
    override val dlqStream = StreamNames.TEST_REQUESTS_DLQ

    override fun handle(payload: String) {
        val msg = objectMapper.readValue(payload, TestRequestMessage::class.java)
        val res = execution.execute(ExecuteRequest(msg.language, msg.version, msg.content, msg.inputs))
        val status = resolveStatus(res, msg)
        val result =
            TestResultMessage(
                msg.jobId,
                msg.snippetId,
                msg.snippetVersion,
                msg.testId,
                status,
                res.outputs,
                res.errors.firstOrNull(),
            )
        producer.publish(StreamNames.TEST_RESULTS, result)
    }
}

private fun resolveStatus(
    res: org.printscript.service.execute.ExecuteResponse,
    msg: TestRequestMessage,
): String =
    when {
        res.errors.isNotEmpty() -> "ERROR"
        res.outputs == msg.expectedOutputs -> "PASSED"
        else -> "FAILED"
    }

private fun Map<String, Any?>.toObjectNode(objectMapper: ObjectMapper): ObjectNode =
    if (isEmpty()) objectMapper.createObjectNode() else objectMapper.valueToTree(this)
