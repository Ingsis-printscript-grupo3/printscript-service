package org.printscript.service.execute

import org.junit.jupiter.api.Test
import org.printscript.service.common.postJson
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc

@SpringBootTest
@AutoConfigureMockMvc
class ExecuteControllerTest(
	@Autowired private val mockMvc: MockMvc,
) {
	@Test
	fun `each println becomes an output in order`() {
		mockMvc
			.postJson("/executions", """{"language":"printscript","version":"1.0","content":"println(1 + 2);\nprintln(\"hi\");"}""")
			.andExpect {
				status { isOk() }
				jsonPath("$.outputs.length()") { value(2) }
				jsonPath("$.outputs[0]") { value("3") }
				jsonPath("$.outputs[1]") { value("hi") }
				jsonPath("$.errors.length()") { value(0) }
			}
	}

	@Test
	fun `inputs are consumed in the order they were sent`() {
		val content = """let a: string = readInput(\"\");\nlet b: string = readInput(\"\");\nprintln(a + b);"""
		mockMvc
			.postJson("/executions", """{"language":"printscript","version":"1.1","content":"$content","inputs":["x","y"]}""")
			.andExpect {
				status { isOk() }
				jsonPath("$.outputs[0]") { value("xy") }
				jsonPath("$.errors.length()") { value(0) }
			}
	}

	@Test
	fun `a failure keeps the outputs printed before it`() {
		val content = """println(\"before\");\nlet a: string = readInput(\"\");"""
		mockMvc
			.postJson("/executions", """{"language":"printscript","version":"1.1","content":"$content"}""")
			.andExpect {
				status { isOk() }
				jsonPath("$.outputs[0]") { value("before") }
				jsonPath("$.errors.length()") { value(1) }
			}
	}

	@Test
	fun `an unsupported version returns 400`() {
		mockMvc
			.postJson("/executions", """{"language":"printscript","version":"9.9","content":"println(1);"}""")
			.andExpect { status { isBadRequest() } }
	}
}
