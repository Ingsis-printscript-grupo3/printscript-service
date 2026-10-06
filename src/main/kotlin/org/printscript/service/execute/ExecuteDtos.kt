package org.printscript.service.execute

import org.printscript.service.common.ErrorDetail

// POST /executions. Los inputs vienen TODOS de antemano y en orden (US#8).
// No hay campo env a proposito: el servicio corre siempre con un env vacio, asi un snippet
// no puede leerse las variables de entorno del contenedor.

data class ExecuteRequest(
	val language: String,
	val version: String,
	val content: String,
	val inputs: List<String> = emptyList(),
)

// outputs trae lo que alcanzo a imprimirse, aunque despues haya fallado
data class ExecuteResponse(
	val outputs: List<String>,
	val errors: List<ErrorDetail>,
)
