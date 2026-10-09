package org.printscript.service.format

import org.junit.jupiter.api.Test
import org.printscript.service.common.postJson
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc

private const val UNFORMATTED_SNIPPET = "let x:number=1;println(x);"

@SpringBootTest
@AutoConfigureMockMvc
class FormatControllerTest(
	@Autowired private val mockMvc: MockMvc,
) {
	@Test
	fun `without rules the snippet comes back untouched`() {
		mockMvc
			.postJson("/formattings", """{"language":"printscript","version":"1.0","content":"$UNFORMATTED_SNIPPET"}""")
			.andExpect {
				status { isOk() }
				jsonPath("$.content") { value(UNFORMATTED_SNIPPET) }
				jsonPath("$.errors.length()") { value(0) }
			}
	}

	@Test
	fun `the rules that were sent are applied`() {
		val rules =
			"""{"enforce-spacing-after-colon-in-declaration":true,"enforce-spacing-around-equals":true,"mandatory-line-break-after-statement":true}"""
		mockMvc
			.postJson("/formattings", """{"language":"printscript","version":"1.0","content":"$UNFORMATTED_SNIPPET","rules":$rules}""")
			.andExpect {
				status { isOk() }
				jsonPath("$.content") { value("let x: number = 1;\nprintln(x);") }
				jsonPath("$.errors.length()") { value(0) }
			}
	}

	@Test
	fun `a snippet that does not parse returns 200 with null content`() {
		mockMvc
			.postJson("/formattings", """{"language":"printscript","version":"1.0","content":"let x: number = ;"}""")
			.andExpect {
				status { isOk() }
				jsonPath("$.content") { value(null) }
				jsonPath("$.errors.length()") { value(1) }
			}
	}

	@Test
	fun `an out of range rule returns 400`() {
		val body =
			"""{"language":"printscript","version":"1.0","content":"$UNFORMATTED_SNIPPET","rules":{"line-breaks-after-println":7}}"""
		mockMvc
			.postJson("/formattings", body)
			.andExpect { status { isBadRequest() } }
	}

	@Test
	fun `contradictory rules return 400`() {
		val body =
			"""{"language":"printscript","version":"1.1","content":"$UNFORMATTED_SNIPPET","rules":{"if-brace-same-line":true,"if-brace-below-line":true}}"""
		mockMvc
			.postJson("/formattings", body)
			.andExpect { status { isBadRequest() } }
	}
}
