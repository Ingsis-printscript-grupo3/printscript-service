package org.printscript.service.validate

import org.printscript.service.common.DiscardOutput
import org.printscript.service.common.toErrorDetail
import org.springframework.stereotype.Service
import printscript.common.LanguageVersion
import printscript.interpreter.env.MapEnvProvider
import printscript.interpreter.input.QueueInput
import printscript.runner.Engine
import printscript.runner.ExecutionResult

@Service
class ValidationService {
	fun validate(request: ValidateRequest): ValidateResponse {
		val version = LanguageVersion.parse(request.version)
		// validar corre el pipeline entero MENOS interpretar: no imprime nada ni pide inputs
		val engine = Engine(DiscardOutput, QueueInput(), MapEnvProvider())
		return when (val result = engine.validate(request.content, version)) {
			is ExecutionResult.Success -> ValidateResponse(valid = true, errors = emptyList())
			is ExecutionResult.Failure -> ValidateResponse(valid = false, errors = listOf(result.toErrorDetail()))
		}
	}
}
