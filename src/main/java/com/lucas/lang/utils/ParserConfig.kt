package com.lucas.lang.utils

/**
 * File ParserConfig.kt
 * Date 2021/8/26
 * Author lucas
 * Introduction 初始化配置
 */
class ParserConfig(
    //项目名称
    val projectName: String,
    //项目路径
    val projectPath: String,
    //excel路径
    var excelPath: String,
    //是否覆盖已存在的内容
    val isCoverLangValues: Boolean = true,
    //是否跳过重复key
//    val isBreakRepeatKeyIndex: Boolean = false,
    //支持的语言类型简写
    val langTypes: MutableList<String> = arrayListOf("zh", "en", "ar", "es", "pt"),
    //excel -> project 是否自动补充缺失的语言类型文件夹以及文件
//    val autoComplete: Boolean = true,

    //语言文件夹匹配正则
    val dirPattern: String = "(values-\\w{2,3})|(values)"
) {
    companion object {
        const val excelModuleRowName = "Module"
        const val excelKeyRowName = "Index"
        const val excelDefRowName = "Default"
        const val excelRemarkRowName = "备注"
        const val defExcelName = "国际化翻译.xls"
        const val indexModuleCell = 0
        const val indexKeyCell = 1
        const val indexDefCell = 2

        //是否开启调试模式
        const val enableLog: Boolean = true

        const val remark = "请注意： %s 和 %d 为占位符\n" +
                "例如： \"一共%s条记录\"，可以代表 \"一共5条记录\"，翻译为 \"%s records in total\" 样式即可\n" +
                "标题类型：\n" +
                "\tModule->模块名称(请勿改动)\n" +
                "\tIndex->主键名称(请勿改动)\n" +
                "\tDefault->默认语言(一般是中文或者英文)\n" +
                "\tzh->中文\n" +
                "\ten->英文\n" +
                "\tar->阿拉伯语\n" +
                "\tes->西班牙语\n" +
                "\tpt->葡萄牙语\n" +
                "\t备注->备注信息\n"
    }
}