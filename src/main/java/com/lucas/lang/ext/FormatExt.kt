package com.lucas.lang.ext


import java.util.regex.Pattern

fun Long.formatTime(): String {
    val l = this / 1000
    val h = l / 3600
    val m = l % 3600 / 60
    val s = l % 3600 % 60
    return "$h 小时 $m 分 $s 秒"
}

//是否需要用CDATA
private val isNeedCdataPattern = "<\\/?.+?>"
private val isCdataPattern = "^<!\\[CDATA\\[.+]]>\$"

//简单错误校验
private val sPattern = mapOf(
    "(% [sS])|(%S)" to "%s",
    "(% [dD])|(%D)" to "%d",
    "(% [fF])|(%F)" to "%f",
)

//特殊-例如阿拉伯语需要对s%做反转
private val sPatternSpacial = mapOf(
    "(٪ [sS])|(٪[sS])|(%s)" to "s%",
    "(٪ [dD])|(٪[dD])|(%d)" to "d%",
    "(٪ [fF])|(٪[fF])|(%f)" to "f%",
)

//复杂的错误校验
private val sPatternPlus = mapOf(
    "% [1-9] \\\$ [sS]" to "%1s\$s"
)

//格式化 % s % d 异常字符
fun String.stringChartFormat(langType: String): String {
    var temp = this
    sPattern.forEach {
        temp = Pattern.compile(it.key).matcher(temp).replaceAll(it.value)
    }
    if (langType in arrayOf("ar", "iw")) {
        sPatternSpacial.forEach {
            temp = Pattern.compile(it.key).matcher(temp).replaceAll(it.value)
        }
    }
    //转译符号替换<![CDATA[ ]]>
    val find = Pattern.compile(isNeedCdataPattern).matcher(temp).find()
    val isCData = Pattern.compile(isCdataPattern).matcher(temp).find()
    if (find && !isCData) {
        temp = "<![CDATA[$temp]]>"
    }
    return temp
}