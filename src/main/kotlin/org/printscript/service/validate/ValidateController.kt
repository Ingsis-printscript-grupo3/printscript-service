package org.printscript.service.validate

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class ValidateController(private val validation: ValidationService) {
	// el recurso es la validacion, en sustantivo: lo que se crea es el resultado, no la accion.
	// un snippet invalido NO es un error HTTP: devuelve 200 con valid=false
	@PostMapping("/validations")
	fun validate(
		@RequestBody request: ValidateRequest,
	): ValidateResponse = validation.validate(request)
}
