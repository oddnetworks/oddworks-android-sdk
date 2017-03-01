package io.oddworks.device.exception

import io.oddworks.device.model.OddError
import java.util.*

/**
 * Response code returned from server was not what was expected
 */
class BadResponseCodeException : OddRequestException {
    val code: Int
    val oddErrors: LinkedHashSet<OddError>

    constructor(code: Int) {
        this.code = code
        this.oddErrors = linkedSetOf()
    }

    constructor(code: Int, oddErrors: LinkedHashSet<OddError>) {
        this.code = code
        this.oddErrors = oddErrors
    }
}
