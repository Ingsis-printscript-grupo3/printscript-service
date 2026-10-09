package org.printscript.service.common

import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActionsDsl
import org.springframework.test.web.servlet.post

// los cuatro endpoints son POST con un body JSON
fun MockMvc.postJson(
	path: String,
	body: String,
): ResultActionsDsl =
	post(path) {
		contentType = MediaType.APPLICATION_JSON
		content = body
	}
