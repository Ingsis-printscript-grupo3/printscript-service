package org.printscript.service.common

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

// vale para TODOS los controllers. Si viviera dentro de uno, los demas no lo heredarian
// y una version invalida les daria 500 en vez de 400.
@RestControllerAdvice
class ApiExceptionHandler {
	// una version que no existe es un error de protocolo: el cliente pidio algo imposible
	@ExceptionHandler(IllegalArgumentException::class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	fun unsupportedVersion(error: IllegalArgumentException): Map<String, String> =
		mapOf("error" to (error.message ?: "Bad request"))
}
