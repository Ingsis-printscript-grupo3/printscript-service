package org.printscript.service.common

import printscript.common.LanguageVersion

private const val PRINTSCRIPT = "printscript"

// un lenguaje o una version que no existen cortan con IllegalArgumentException, que sale como 400
fun resolveVersion(
    language: String,
    version: String,
): LanguageVersion {
    require(language.equals(PRINTSCRIPT, ignoreCase = true)) { "Unsupported language: $language" }
    return LanguageVersion.parse(version)
}
