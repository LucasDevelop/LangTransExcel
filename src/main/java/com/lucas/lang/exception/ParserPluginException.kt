package com.lucas.lang.exception

import java.lang.RuntimeException

class ParserPluginException:RuntimeException {
    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
}