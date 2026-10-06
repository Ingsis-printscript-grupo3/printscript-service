package org.printscript.service.common

import printscript.runner.ExecutionResult

// lo comparten los cuatro endpoints: una posicion de PrintScript es (linea, columna),
// y un error tiene dos, donde empieza y donde termina
data class ErrorDetail(
	val message: String,
	val line: Int,
	val column: Int,
	val endLine: Int,
	val endColumn: Int,
)

// unico traductor de un error de PrintScript al JSON que ve el usuario.
// start y end pueden ser null, y entonces no hay posicion que mostrar.
fun ExecutionResult.Failure.toErrorDetail(): ErrorDetail =
	ErrorDetail(
		message = message,
		line = start?.line ?: 0,
		column = start?.column ?: 0,
		endLine = end?.line ?: 0,
		endColumn = end?.column ?: 0,
	)
