package com.lucas.lang.v2.confoig

import com.lucas.lang.v2.bean.RowBean
import com.lucas.lang.v2.bean.SheetBean
import java.util.*

//导入配置
class InputConfig : IConfig() {
    var sheetNames = LinkedList<SheetBean>()//所有sheet 名称
    var selectSheet: SheetBean? = null//选择需要解析的sheet
    var rows: List<RowBean>? = null
    var defValueLangName: String? = null//默认value文件的语言类型
    var isAutoCompletionDirOrFile: Boolean = true//是否自动补全缺失的文件夹和文件
//    var isAutoCompletionKey: Boolean = true//是否自动补全缺失的key
    var isSmartFix: Boolean = true//是否开启智能修复
}