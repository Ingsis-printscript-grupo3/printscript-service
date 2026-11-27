package org.printscript.service.rules

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class RuleController(
    private val catalog: RuleCatalogService,
) {
    @GetMapping("/rules")
    fun list(
        @RequestParam type: String,
    ): List<RuleDefinition> {
        val ruleType =
            RuleType.entries.firstOrNull { it.name.equals(type, ignoreCase = true) }
                ?: throw IllegalArgumentException("Tipo de regla inválido: '$type'")
        return catalog.list(ruleType)
    }
}
