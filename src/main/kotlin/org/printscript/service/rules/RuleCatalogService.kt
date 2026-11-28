package org.printscript.service.rules

import org.springframework.stereotype.Service

@Service
class RuleCatalogService {
    fun list(type: RuleType): List<RuleDefinition> =
        when (type) {
            RuleType.FORMAT -> FORMAT_RULES
            RuleType.LINT -> LINT_RULES
        }

    companion object {
        private val FORMAT_RULES =
            listOf(
                intRule("line-breaks-after-println", defaultValue = "0", min = 0, max = 2),
                boolRule("mandatory-line-break-after-statement"),
                boolRule("if-brace-same-line", exclusiveWith = listOf("if-brace-below-line")),
                boolRule("if-brace-below-line", exclusiveWith = listOf("if-brace-same-line")),
                boolRule("mandatory-single-space-separation"),
                boolRule("enforce-spacing-before-colon-in-declaration"),
                boolRule("enforce-spacing-after-colon-in-declaration"),
                boolRule("enforce-spacing-around-equals", exclusiveWith = listOf("enforce-no-spacing-around-equals")),
                boolRule("enforce-no-spacing-around-equals", exclusiveWith = listOf("enforce-spacing-around-equals")),
                boolRule("mandatory-space-surrounding-operations"),
                intRule("indent-inside-if", defaultValue = "0", min = 0, max = null),
            )

        private val LINT_RULES =
            listOf(
                RuleDefinition(
                    name = "identifier_format",
                    type = RuleType.LINT,
                    valueType = RuleValueType.ENUM,
                    defaultEnabled = true,
                    defaultValue = "camel case",
                    allowedValues = listOf("camel case", "snake case"),
                ),
                RuleDefinition(
                    name = "mandatory-variable-or-literal-in-println",
                    type = RuleType.LINT,
                    valueType = RuleValueType.BOOLEAN,
                    defaultEnabled = true,
                ),
                RuleDefinition(
                    name = "mandatory-variable-or-literal-in-readInput",
                    type = RuleType.LINT,
                    valueType = RuleValueType.BOOLEAN,
                    defaultEnabled = true,
                    onlyVersions = listOf("1.1"),
                ),
            )

        private fun boolRule(
            name: String,
            exclusiveWith: List<String> = emptyList(),
        ): RuleDefinition =
            RuleDefinition(
                name = name,
                type = RuleType.FORMAT,
                valueType = RuleValueType.BOOLEAN,
                defaultEnabled = false,
                exclusiveWith = exclusiveWith,
            )

        private fun intRule(
            name: String,
            defaultValue: String,
            min: Int,
            max: Int?,
        ): RuleDefinition =
            RuleDefinition(
                name = name,
                type = RuleType.FORMAT,
                valueType = RuleValueType.INTEGER,
                defaultEnabled = false,
                defaultValue = defaultValue,
                min = min,
                max = max,
            )
    }
}
