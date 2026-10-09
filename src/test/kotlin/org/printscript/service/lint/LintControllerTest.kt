package org.printscript.service.lint

import org.junit.jupiter.api.Test
import org.printscript.service.common.postJson
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc

private const val SNAKE_CASE_SNIPPET = """let my_var: number = 1;\nprintln(my_var + 2);"""

@SpringBootTest
@AutoConfigureMockMvc
class LintControllerTest(
	@Autowired private val mockMvc: MockMvc,
) {
	@Test
	fun `without rules the default ones run`() {
		mockMvc
			.postJson("/lintings", """{"language":"printscript","version":"1.0","content":"$SNAKE_CASE_SNIPPET"}""")
			.andExpect {
				status { isOk() }
				jsonPath("$.warnings.length()") { value(2) }
				jsonPath("$.warnings[0].line") { value(1) }
				jsonPath("$.warnings[0].column") { value(5) }
				jsonPath("$.warnings[1].line") { value(2) }
				jsonPath("$.errors.length()") { value(0) }
			}
	}

	@Test
	fun `only the rules that were sent are checked`() {
		val body =
			"""{"language":"printscript","version":"1.0","content":"$SNAKE_CASE_SNIPPET","rules":{"identifier_format":"snake case"}}"""
		mockMvc
			.postJson("/lintings", body)
			.andExpect {
				status { isOk() }
				jsonPath("$.warnings.length()") { value(0) }
				jsonPath("$.errors.length()") { value(0) }
			}
	}

	@Test
	fun `a snippet that does not parse returns 200 with the error`() {
		mockMvc
			.postJson("/lintings", """{"language":"printscript","version":"1.0","content":"let x: number = ;"}""")
			.andExpect {
				status { isOk() }
				jsonPath("$.warnings.length()") { value(0) }
				jsonPath("$.errors.length()") { value(1) }
				jsonPath("$.errors[0].line") { value(1) }
			}
	}

	@Test
	fun `an invalid rule value returns 400`() {
		val body =
			"""{"language":"printscript","version":"1.0","content":"let x: number = 1;","rules":{"identifier_format":"kebab"}}"""
		mockMvc
			.postJson("/lintings", body)
			.andExpect { status { isBadRequest() } }
	}
}
