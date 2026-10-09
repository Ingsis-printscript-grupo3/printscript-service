package org.printscript.service.lint

import org.printscript.service.common.ErrorDetail
import tools.jackson.databind.node.ObjectNode

// POST /lintings. rules usa las mismas claves que el config de PrintScript
// si no viene, corren las reglas predeterminadas
data class LintRequest(
	val language: String,
	val version: String,
	val content: String,
	val rules: ObjectNode? = null,
)

data class LintWarning(
	val message: String,
	val line: Int,
	val column: Int,
)

// warnings son reglas de estilo incumplidas y errors es que el snippet ni parsea
data class LintResponse(
	val warnings: List<LintWarning>,
	val errors: List<ErrorDetail>,
)
