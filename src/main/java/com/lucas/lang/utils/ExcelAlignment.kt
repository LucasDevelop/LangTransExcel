package com.lucas.lang.utils

import com.lucas.lang.exception.ParserPluginException
import org.apache.poi.hssf.usermodel.HSSFRichTextString
import org.apache.poi.hssf.usermodel.HSSFRow
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Row
import java.io.File

/**
 * File ExcelAlignment.kt
 * Date 2021/9/9
 * Author lucas
 * Introduction 将两个excel文件进行对齐
 */
object ExcelAlignment {
    private const val originSheetIndex = 0
    private const val originKeyCellIndex = 0
    private const val templateSheetIndex = 0
    private const val templateKeyCellIndex = 1

    fun alignment(templateExcelFile: String, originExcelFile: String) {
        if (!File(templateExcelFile).exists()) throw ParserPluginException("$templateExcelFile 文件不存在！")
        if (!File(originExcelFile).exists()) throw ParserPluginException("$originExcelFile 文件不存在！")
        val templateWorkbook = HSSFWorkbook(File(templateExcelFile).inputStream())
        val originWorkbook = HSSFWorkbook(File(originExcelFile).inputStream())
        val originSheet = originWorkbook.getSheetAt(originSheetIndex)
        val templateSheet = templateWorkbook.getSheetAt(templateSheetIndex)
        for (i in 2 until originSheet.lastRowNum) {
            val originRow = originSheet.getRow(i)
            val originIndexName = originRow.getCell(originKeyCellIndex).richStringCellValue.string
            //查找出相同key--可能存在多个相同的key
            templateSheet.forEach {
                val templateKeyName = it.getCell(templateKeyCellIndex)?.richStringCellValue?.string
                if (templateKeyName != null && templateKeyName == originIndexName) {
                    //覆盖value
                    coverValue(originRow, it, 1, 3)
                    coverValue(originRow, it, 2, 2)
                    coverValue(originRow, it, 2, 4)
                }
            }
        }
        templateWorkbook.write(File(templateExcelFile).outputStream())
    }

    private fun coverValue(originRow: HSSFRow, findRow: Row, originCellValueIndex: Int, tempCellValueIndex: Int) {
        val string = originRow.getCell(originCellValueIndex)?.richStringCellValue?.string
        if (string.isNullOrEmpty()) {//模版值为空就跳过
            return
        }
        val cell = findRow.getCell(tempCellValueIndex) ?: findRow.createCell(tempCellValueIndex)
        cell.setCellValue(HSSFRichTextString(string))
    }
}


















