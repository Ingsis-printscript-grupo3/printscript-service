package org.printscript.service.format

import org.printscript.service.common.ErrorDetail
import tools.jackson.databind.node.ObjectNode

// POST /formattings. rules usa las mismas claves que el config de PrintScript
// si no viene, no hay reglas activas y el snippet vuelve como estaba
data class FormatRequest(
	val language: String,
	val version: String,
	val content: String,
	val rules: ObjectNode? = null,
)

// content es null cuando el snippet no parsea: no hay version formateada que devolver
data class FormatResponse(
	val content: String?,
	val errors: List<ErrorDetail>,
)
