package com.lucas.lang.v2.confoig

import com.lucas.lang.v2.bean.ModuleBean

open class IConfig {
    var projectName: String = ""//项目名称
    var projectPath: String = ""//项目地址
    var allModuleBeans: List<ModuleBean>? = null//所有module
    var selectModuleBeans: List<ModuleBean>? = null//选择的module
    var allLangs: List<String>? = null//所有语言
    var selectLangs: List<String>? = null
    var excelPath:String = ""//excel文件路径
}