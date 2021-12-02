package com.lucas.lang.v2.confoig

object Constant {
    const val FIELD_PROPERTY = "property"
    const val DEF_LANG = "defLang"

    //语言类型语字符的映射关系
    val langMap = mapOf(
        "zh" to "中文",
        "zh_CN" to "中文-中国",
        "zh_TW" to "中文-台湾",
        "en" to "英文",
        "ar" to "阿拉伯语",
        "pt" to "葡萄牙语",
        "es" to "墨西哥语",
    )

    //百度翻译配置
     val baiduAppId = "20210924000954974"
     val baiduKey = "4C8QDD77vcxKVjwTCJsn"

    //百度语言类型与Android的语言类型映射关系
     val baiduLangTypeMap = mapOf(
        "zh" to "zh",
        "en" to "en",
        "ar" to "ara",
        "es" to "spa",
        "pt" to "pt",
    )


}