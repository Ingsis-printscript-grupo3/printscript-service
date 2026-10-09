package org.printscript.service.lint

import org.printscript.service.common.DiscardOutput
import org.printscript.service.common.toErrorDetail
import org.springframework.stereotype.Service
import printscript.common.LanguageVersion
import printscript.interpreter.env.MapEnvProvider
import printscript.interpreter.input.QueueInput
import printscript.linter.LinterFactory
import printscript.runner.Engine
import printscript.runner.LintResult
import java.io.StringReader

@Service
class LintingService {
	fun lint(request: LintRequest): LintResponse {
		val version = LanguageVersion.parse(request.version)
		val linter =
			request.rules?.let { LinterFactory.fromJson(it.toString(), version) }
				?: LinterFactory.create(version)
		val warnings = mutableListOf<LintWarning>()
		val engine = Engine(DiscardOutput, QueueInput(), MapEnvProvider())
		val result =
			engine.lint(StringReader(request.content), version) { statements ->
				linter.analyze(statements) { warning ->
					warnings.add(LintWarning(warning.message, warning.position.line, warning.position.column))
				}
			}
		return when (result) {
			is LintResult.Success -> LintResponse(warnings, emptyList())
			// aunq falle, se devuelven los warnings que se juntaron hasta antes
			is LintResult.Failure -> LintResponse(warnings, listOf(result.toErrorDetail()))
		}
	}
}
