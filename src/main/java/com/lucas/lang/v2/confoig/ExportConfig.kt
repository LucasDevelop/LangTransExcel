package com.lucas.lang.v2.confoig

import com.lucas.lang.v2.bean.ModuleBean

//导出配置
class ExportConfig : IConfig() {
    var isEnableOnlineTrans = false//是否开启在线翻译
    var onlineTransLangType: String? = null//在线翻译模版类型
}
