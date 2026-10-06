package org.printscript.service.common

import printscript.interpreter.output.Output

// el Engine siempre pide un Output, incluso cuando la operacion no interpreta nada
object DiscardOutput : Output {
	override fun emit(line: String) = Unit
}

// cada println del snippet cae aca, en orden. Es el "outputs" de la respuesta de /executions.
class CollectingOutput : Output {
	private val collected = mutableListOf<String>()
	val lines: List<String> get() = collected

	override fun emit(line: String) {
		collected.add(line)
	}
}
