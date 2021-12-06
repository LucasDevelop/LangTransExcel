package com.lucas.lang.v2.utils

import com.alibaba.excel.EasyExcelFactory
import com.alibaba.excel.context.AnalysisContext
import com.alibaba.excel.event.AnalysisEventListener
import com.alibaba.excel.metadata.data.ReadCellData
import com.alibaba.excel.read.listener.ModelBuildEventListener
import com.alibaba.excel.read.listener.ReadListener
import com.alibaba.excel.read.metadata.ReadSheet
import com.alibaba.excel.write.handler.SheetWriteHandler
import com.alibaba.excel.write.metadata.WriteSheet
import com.alibaba.excel.write.metadata.WriteTable
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder
import com.alibaba.excel.write.metadata.holder.WriteWorkbookHolder
import com.lucas.lang.ext.log
import com.lucas.lang.v2.bean.RowBean
import com.lucas.lang.v2.confoig.Constant
import com.lucas.lang.v2.confoig.ExportConfig
import com.lucas.lang.v2.confoig.InputConfig
import java.io.File

/**
 * excel解析工具,基于ali easyExcel
 */
object ExcelUtil {

    fun getSheetList(excelPath: String): MutableList<ReadSheet>? {
        val file = File(excelPath)
        if (!file.exists()) return null
        val build = EasyExcelFactory.read(file).build()
        val sheetList = build.excelExecutor().sheetList()
        build.finish()
        return sheetList
    }

    fun readRowsBySheetName(
        inputConfig: InputConfig,
        sheetIndex: Int?,
        progress: (RowBean) -> Unit,
        error: (msg: String) -> Unit,
        complete: (List<RowBean>) -> Unit
    ) {
        val file = File(inputConfig.excelPath)
        if (!file.exists()) return
        EasyExcelFactory.read(file, object : AnalysisEventListener<HashMap<Int,String>>() {
            private val datas = ArrayList<RowBean>()
            private var allHeads = mutableListOf<String>()

            override fun invoke(cellDataMap: HashMap<Int, String>?, context: AnalysisContext?) {
                //读取head
                val b = cellDataMap?.any { it.value == Constant.FIELD_PROPERTY } == true
                if (context?.readRowHolder()?.rowIndex == 1 && b) {
                    allHeads = cellDataMap!!.map { it.value }.toMutableList()
                    inputConfig.allLangs = cellDataMap.filter { it.value != Constant.FIELD_PROPERTY }
                        .map { it.value }
                    return
                }
                if (inputConfig.allLangs.isNullOrEmpty()) {
                    error("未读取到head属性!")
                    return
                }
                //读取翻译内容
                if (!cellDataMap.isNullOrEmpty() && !inputConfig.allLangs.isNullOrEmpty()) {
                    val rowBean = RowBean()
                    allHeads.forEach {
                        val langValue = cellDataMap[allHeads.indexOf(it)]
                        if (!langValue.isNullOrBlank())
                            rowBean.langs[it] = langValue
                    }
                    progress(rowBean)
                    rowBean.rowAttr
                    datas.add(rowBean)
                }
            }

            override fun doAfterAllAnalysed(context: AnalysisContext?) {
                complete(datas)
            }
        }).ignoreEmptyRow(true)
            .sheet(sheetIndex).doRead()
    }

    fun writeDataBySheetName(excelPath: String, sheetName: String?, exportConfig: ExportConfig, data: List<RowBean>) {
        val file = File(excelPath)
        if (!file.exists()){
            file.createNewFile()
        }
        //创建sheet
        val writeSheet = WriteSheet()
        writeSheet.sheetName = sheetName
        writeSheet.sheetNo =2
        //创建表头
        val writeTable = WriteTable()
        writeTable.tableNo = 1
        val headData = mutableListOf(
            mutableListOf("字段属性(请勿修改内容)", Constant.FIELD_PROPERTY),
        )
        val langHead = exportConfig.selectLangs?.map {
            mutableListOf(Constant.langMap[it]!!, it)
        }!!.toMutableList()
        headData.addAll(langHead)
        writeTable.head = headData

        //填入数据
        val rowData = data.map { row ->
            mutableListOf<String>().apply {
                headData.forEach { lang ->
                    val langName = lang[1]
                    if (row.langs.containsKey(langName)) {
                        add(row.langs[langName]!!)
                    }
                }
            }
        }.toMutableList()
        EasyExcelFactory
            .write(file)
            .registerWriteHandler(object : SheetWriteHandler {
                override fun afterSheetCreate(
                    writeWorkbookHolder: WriteWorkbookHolder?,
                    writeSheetHolder: WriteSheetHolder?
                ) {
                    //冻结第一行
                    super.afterSheetCreate(writeWorkbookHolder, writeSheetHolder)
                    writeSheetHolder?.sheet?.createFreezePane(0, 1, 0, 1)
                }
            })
            .build()
            .write(rowData, writeSheet, writeTable)
            .finish()
    }
}