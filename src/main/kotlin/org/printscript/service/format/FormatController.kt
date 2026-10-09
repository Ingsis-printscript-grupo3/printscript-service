package org.printscript.service.format

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class FormatController(private val formatting: FormattingService) {
	@PostMapping("/formattings")
	fun format(
		@RequestBody request: FormatRequest,
	): FormatResponse = formatting.format(request)
}
