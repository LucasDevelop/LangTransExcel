package com.lucas.lang.v2.bean

/**
 * 单行数据的属性
 */
class RowAttr {
    var moduleName: String? = null//模块名称
    var keyName: String? = null//字段名称
    var isArray: Boolean = false//是否是数组
    var arrayItemIndex: Int = -1//数组item索引
    override fun toString(): String {
        return "RowAttr(moduleName=$moduleName, keyName=$keyName)"
    }

}