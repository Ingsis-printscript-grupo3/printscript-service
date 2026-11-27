package org.printscript.service.rules

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@WebMvcTest(RuleController::class)
@Import(RuleCatalogService::class)
class RuleControllerTest(
    @Autowired private val mockMvc: MockMvc,
) {
    @Test
    fun `returns format rules with defaults and constraints`() {
        mockMvc
            .get("/rules?type=FORMAT")
            .andExpect {
                status { isOk() }
                jsonPath("$[?(@.name == 'line-breaks-after-println')].min") { value(0) }
                jsonPath("$[?(@.name == 'line-breaks-after-println')].max") { value(2) }
                jsonPath("$[?(@.name == 'if-brace-same-line')].exclusiveWith[0]") { value("if-brace-below-line") }
            }
    }

    @Test
    fun `returns lint rules with defaults`() {
        mockMvc
            .get("/rules?type=LINT")
            .andExpect {
                status { isOk() }
                jsonPath("$[?(@.name == 'identifier_format')].defaultValue") { value("camel case") }
                jsonPath("$[?(@.name == 'mandatory-variable-or-literal-in-readInput')].onlyVersions[0]") {
                    value("1.1")
                }
            }
    }

    @Test
    fun `rejects invalid rule type`() {
        mockMvc
            .get("/rules?type=INVALID")
            .andExpect {
                status { isBadRequest() }
            }
    }
}
