package com.lucas.lang.bean

/**
 * File LangElement.kt
 * Date 2021/8/26
 * Author lucas
 * Introduction
 */
class RowElement(
    val key: String,
    val langElements: List<LangElement>,
    val moduleName: String,
) {
    override fun toString(): String {
        return "RowElement(key='$key', langElements=$langElements, moduleName='$moduleName')"
    }
}