package org.printscript.service.execute

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class ExecuteController(private val execution: ExecutionService) {
	// un snippet que falla en runtime tampoco es un error HTTP: 200 con los errores adentro
	@PostMapping("/executions")
	fun execute(
		@RequestBody request: ExecuteRequest,
	): ExecuteResponse = execution.execute(request)
}
