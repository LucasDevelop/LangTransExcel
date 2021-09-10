package com.lucas.lang.bean

class LangElement(
    val rowName:String,
    val cellValue:String,
//    val langType:String,
//    val nodeType: Short,
    var status: ElementStatus = ElementStatus.NORMAL,
){
    override fun toString(): String {
        return "LangElement(rowName='$rowName', cellValue='$cellValue', status=$status)"
    }
}