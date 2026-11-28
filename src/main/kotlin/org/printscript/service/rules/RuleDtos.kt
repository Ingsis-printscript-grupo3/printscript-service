package org.printscript.service.rules

enum class RuleType {
    LINT,
    FORMAT,
}

enum class RuleValueType {
    BOOLEAN,
    INTEGER,
    ENUM,
}

data class RuleDefinition(
    val name: String,
    val type: RuleType,
    val valueType: RuleValueType,
    val defaultEnabled: Boolean,
    val defaultValue: String? = null,
    val min: Int? = null,
    val max: Int? = null,
    val allowedValues: List<String> = emptyList(),
    val exclusiveWith: List<String> = emptyList(),
    val onlyVersions: List<String> = emptyList(),
)
