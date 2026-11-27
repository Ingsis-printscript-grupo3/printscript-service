package org.printscript.service.streams

import org.junit.jupiter.api.Test
import org.printscript.service.execute.ExecutionService
import org.printscript.service.format.FormattingService
import org.printscript.service.lint.LintingService
import org.springframework.data.redis.connection.stream.RecordId
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.jsonMapper
import tools.jackson.module.kotlin.kotlinModule
import java.time.Instant
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WorkerHandlersTest {
    private val objectMapper: ObjectMapper = jsonMapper { addModule(kotlinModule()) }

    private class FakeProducer : StreamProducer {
        val published = mutableListOf<Pair<String, Any>>()

        override fun publish(
            stream: String,
            payload: Any,
        ): RecordId {
            published.add(stream to payload)
            return RecordId.of("1-0")
        }
    }

    @Test
    fun `lint worker publishes warnings with custom rules`() {
        val producer = FakeProducer()
        val handler = LintWorkerHandler(LintingService(), producer, objectMapper)
        val msg =
            LintRequestMessage(
                jobId = UUID.randomUUID(),
                userId = "u1",
                snippetId = UUID.randomUUID(),
                snippetVersion = Instant.now(),
                language = "printscript",
                version = "1.0",
                content = "let my_var: number = 1;\nprintln(1);",
                rules = mapOf("identifier_format" to "camel case"),
            )

        handler.handle(objectMapper.writeValueAsString(msg))

        assertEquals(1, producer.published.size)
        val (stream, result) = producer.published.first()
        assertEquals(StreamNames.LINT_RESULTS, stream)
        val lintRes = result as LintResultMessage
        assertEquals(1, lintRes.warnings.size)
        assertTrue(lintRes.errors.isEmpty())
    }

    @Test
    fun `lint worker with empty rules disables all lint rules`() {
        val producer = FakeProducer()
        val handler = LintWorkerHandler(LintingService(), producer, objectMapper)
        val msg =
            LintRequestMessage(
                jobId = UUID.randomUUID(),
                userId = "u1",
                snippetId = UUID.randomUUID(),
                snippetVersion = Instant.now(),
                language = "printscript",
                version = "1.0",
                content = "let my_var: number = 1;\nprintln(1 + 2);",
                rules = emptyMap(),
            )

        handler.handle(objectMapper.writeValueAsString(msg))

        val lintRes = producer.published.single().second as LintResultMessage
        assertTrue(lintRes.warnings.isEmpty())
        assertTrue(lintRes.errors.isEmpty())
    }

    @Test
    fun `format worker publishes formatted content`() {
        val producer = FakeProducer()
        val handler = FormatWorkerHandler(FormattingService(), producer, objectMapper)
        val msg =
            FormatRequestMessage(
                jobId = UUID.randomUUID(),
                userId = "u1",
                snippetId = UUID.randomUUID(),
                snippetVersion = Instant.now(),
                language = "printscript",
                version = "1.0",
                content = "let x:number=5;",
                rules = mapOf("enforce-spacing-around-equals" to true),
            )

        handler.handle(objectMapper.writeValueAsString(msg))

        val (stream, result) = producer.published.first()
        assertEquals(StreamNames.FORMAT_RESULTS, stream)
        val formatRes = result as FormatResultMessage
        assertEquals("let x:number = 5;", formatRes.formattedContent?.trim())
    }

    @Test
    fun `test worker evaluates outputs and reports PASSED or FAILED`() {
        val producer = FakeProducer()
        val handler = TestWorkerHandler(ExecutionService(), producer, objectMapper)
        val snippetId = UUID.randomUUID()
        val testId = UUID.randomUUID()

        val passMsg =
            TestRequestMessage(
                userId = "u1",
                snippetId = snippetId,
                snippetVersion = Instant.now(),
                testId = testId,
                language = "printscript",
                version = "1.0",
                content = "let x: number = 5;\nprintln(x * 2);",
                inputs = emptyList(),
                expectedOutputs = listOf("10"),
            )
        handler.handle(objectMapper.writeValueAsString(passMsg))

        val (stream, result) = producer.published.first()
        assertEquals(StreamNames.TEST_RESULTS, stream)
        val testRes = result as TestResultMessage
        assertEquals("PASSED", testRes.status)
        assertEquals(listOf("10"), testRes.outputs)
    }
}
