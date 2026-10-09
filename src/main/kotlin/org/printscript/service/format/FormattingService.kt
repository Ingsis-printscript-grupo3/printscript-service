package org.printscript.service.format

import org.printscript.service.common.DiscardOutput
import org.printscript.service.common.toErrorDetail
import org.springframework.stereotype.Service
import printscript.common.LanguageVersion
import printscript.formatter.Formatter
import printscript.formatter.FormatterRules
import printscript.formatter.FormatterRulesLoader
import printscript.interpreter.env.MapEnvProvider
import printscript.interpreter.input.QueueInput
import printscript.runner.Engine
import printscript.runner.FormatResult
import java.io.StringReader
import java.io.StringWriter

@Service
class FormattingService {
	fun format(request: FormatRequest): FormatResponse {
		val version = LanguageVersion.parse(request.version)
		val rules = request.rules?.let { FormatterRulesLoader.fromJson(it.toString()) } ?: FormatterRules()
		val formatted = StringWriter()
		val engine = Engine(DiscardOutput, QueueInput(), MapEnvProvider())
		// el Engine lee el fuente dos veces, por eso recibe como abrirlo y no un Reader
		val result =
			engine.format({ StringReader(request.content) }, version) { tokens ->
				Formatter(rules).format(tokens, formatted)
			}
		return when (result) {
			is FormatResult.Success -> FormatResponse(formatted.toString(), emptyList())
			is FormatResult.Failure -> FormatResponse(null, listOf(result.toErrorDetail()))
		}
	}
}
