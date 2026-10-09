package org.printscript.service.validate

import org.junit.jupiter.api.Test
import org.printscript.service.common.postJson
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc

@SpringBootTest
@AutoConfigureMockMvc
class ValidateControllerTest(
	@Autowired private val mockMvc: MockMvc,
) {
	@Test
	fun `a valid snippet returns valid true and no errors`() {
		mockMvc
			.postJson("/validations", """{"language":"printscript","version":"1.0","content":"let x: number = 1;"}""")
			.andExpect {
				status { isOk() }
				jsonPath("$.valid") { value(true) }
				jsonPath("$.errors.length()") { value(0) }
			}
	}

	@Test
	fun `a syntax error returns 200 with valid false and its position`() {
		mockMvc
			.postJson("/validations", """{"language":"printscript","version":"1.0","content":"let x: number = ;"}""")
			.andExpect {
				status { isOk() }
				jsonPath("$.valid") { value(false) }
				jsonPath("$.errors.length()") { value(1) }
				jsonPath("$.errors[0].line") { value(1) }
				jsonPath("$.errors[0].column") { value(17) }
			}
	}

	@Test
	fun `a semantic error returns 200 with valid false`() {
		mockMvc
			.postJson("/validations", """{"language":"printscript","version":"1.0","content":"let x: number = \"a\";"}""")
			.andExpect {
				status { isOk() }
				jsonPath("$.valid") { value(false) }
				jsonPath("$.errors[0].message") { value("Incompatible types.") }
			}
	}

	@Test
	fun `an unsupported version returns 400`() {
		mockMvc
			.postJson("/validations", """{"language":"printscript","version":"9.9","content":"let x: number = 1;"}""")
			.andExpect {
				status { isBadRequest() }
				jsonPath("$.error") { value("Unsupported PrintScript version: 9.9") }
			}
	}
}
