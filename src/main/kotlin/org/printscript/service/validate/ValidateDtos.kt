package org.printscript.service.validate

import org.printscript.service.common.ErrorDetail

// el contrato acordado con el equipo: lo que entra y sale de POST /validations

data class ValidateRequest(
	val language: String,
	val version: String,
	val content: String,
)

// errors es una lista aunque hoy traiga un solo elemento: el Engine corta en el primer error.
// Dejarla como lista permite devolver varios mas adelante sin romperle el contrato a nadie.
data class ValidateResponse(
	val valid: Boolean,
	val errors: List<ErrorDetail>,
)
