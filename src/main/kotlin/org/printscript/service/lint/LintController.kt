package org.printscript.service.lint

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class LintController(private val linting: LintingService) {
	@PostMapping("/lintings")
	fun lint(
		@RequestBody request: LintRequest,
	): LintResponse = linting.lint(request)
}
