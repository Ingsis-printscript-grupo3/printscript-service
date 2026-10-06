package org.printscript.service.execute

import org.printscript.service.common.CollectingOutput
import org.printscript.service.common.toErrorDetail
import org.springframework.stereotype.Service
import printscript.common.LanguageVersion
import printscript.interpreter.env.MapEnvProvider
import printscript.interpreter.input.QueueInput
import printscript.runner.Engine
import printscript.runner.ExecutionResult

@Service
class ExecutionService {
	fun execute(request: ExecuteRequest): ExecuteResponse {
		val version = LanguageVersion.parse(request.version)
		val output = CollectingOutput()
		// un Engine nuevo por request: la cola de inputs se vacia al usarse y es de ESTA ejecucion.
		// Compartir la instancia mezclaria los inputs de un usuario con los de otro.
		val engine = Engine(output, QueueInput(request.inputs), MapEnvProvider())
		return when (val result = engine.execute(request.content, version)) {
			is ExecutionResult.Success -> ExecuteResponse(output.lines, emptyList())
			// aun fallando devolvemos lo que se alcanzo a imprimir antes del error
			is ExecutionResult.Failure -> ExecuteResponse(output.lines, listOf(result.toErrorDetail()))
		}
	}
}
