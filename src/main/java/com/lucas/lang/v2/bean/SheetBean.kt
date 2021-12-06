package com.lucas.lang.v2.bean

class SheetBean {
    constructor(sheetIndex: Int?, sheetName: String?) {
        this.sheetIndex = sheetIndex
        this.sheetName = sheetName
    }

    var sheetIndex:Int?=null
    var sheetName:String?=null
    override fun toString(): String {
        return "SheetBean(sheetIndex=$sheetIndex, sheetName=$sheetName)"
    }


}