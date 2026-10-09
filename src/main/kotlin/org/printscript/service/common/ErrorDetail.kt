package org.printscript.service.common

import printscript.common.Position
import printscript.runner.ExecutionResult
import printscript.runner.FormatResult
import printscript.runner.LintResult

// lo comparten los cuatro endpoints: una posicion de PrintScript es (linea, columna),
// y un error tiene dos, donde empieza y donde termina
data class ErrorDetail(
	val message: String,
	val line: Int,
	val column: Int,
	val endLine: Int,
	val endColumn: Int,
)

// el Engine devuelve un Failure distinto por operacion, los tres con la misma forma
fun ExecutionResult.Failure.toErrorDetail(): ErrorDetail = errorDetail(message, start, end)

fun LintResult.Failure.toErrorDetail(): ErrorDetail = errorDetail(message, start, end)

fun FormatResult.Failure.toErrorDetail(): ErrorDetail = errorDetail(message, start, end)

// start y end pueden ser null, y entonces no hay posicion que mostrar
private fun errorDetail(
	message: String,
	start: Position?,
	end: Position?,
): ErrorDetail =
	ErrorDetail(
		message = message,
		line = start?.line ?: 0,
		column = start?.column ?: 0,
		endLine = end?.line ?: 0,
		endColumn = end?.column ?: 0,
	)
