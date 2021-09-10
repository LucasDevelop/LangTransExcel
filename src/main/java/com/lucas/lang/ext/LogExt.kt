package com.lucas.lang.ext

import com.lucas.lang.utils.ParserConfig

var logProxy:(String)->Unit = {}

fun logDividerStartEnd(name: String) {
    log(">>> $name <<<")
}

fun logDividerStart(name: String) {
    log(">>> $name")
}

fun logDividerEnd(name: String) {
    log("<<< $name\n")
}

fun log(content: String) {
    logProxy.invoke(content)
    if (ParserConfig.enableLog)
        println(content)
}