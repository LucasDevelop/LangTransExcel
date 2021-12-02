//package com.lucas.lang.utils
//
//import com.lucas.lang.exception.ParserPluginException
//import com.lucas.lang.ext.formatTime
//import com.lucas.lang.ext.log
//import com.lucas.lang.ext.stringChartFormat
//import org.apache.poi.hssf.usermodel.HSSFWorkbook
//import org.apache.poi.ss.usermodel.Row
//import org.dom4j.Document
//import org.dom4j.DocumentHelper
//import org.dom4j.Element
//import org.dom4j.io.OutputFormat
//import org.dom4j.io.XMLWriter
//import java.io.File
//import java.io.FileOutputStream
//
///**
// * File TransExcelToXml.kt
// * Date 2021/9/10
// * Author lucas
// * Introduction excel 导入 xml
// */
//object TransExcelToXml {
//    private lateinit var parserConfig: ParserConfig
//    private val fileByRootElementMap = mutableMapOf<File, Document>()
//
//    fun initConfig(parserConfig: ParserConfig): TransExcelToXml {
//        TransExcelToXml.parserConfig = parserConfig
//        parserConfig.langTypes = ArrayList(parserConfig.orgLangTypes)
//        //检查参数
//        if (parserConfig.projectName.isEmpty()) throw ParserPluginException("projectName不能为空！")
//        if (parserConfig.projectPath.isEmpty()) throw ParserPluginException("projectPath不能为空！")
//        if (!File(parserConfig.projectPath).exists()) throw ParserPluginException("项目地址：${parserConfig.projectPath}不存在！")
//        if (!File(parserConfig.excelPath).isFile) throw ParserPluginException("Excel地址不是文件类型！")
//        parserConfig.langTypes.add(ParserConfig.indexModuleCell, ParserConfig.excelModuleRowName)
//        parserConfig.langTypes.add(ParserConfig.indexKeyCell, ParserConfig.excelKeyRowName)
//        parserConfig.langTypes.add(ParserConfig.indexDefCell, ParserConfig.excelDefRowName)
//        return this
//    }
//
//    fun start() {
//        val startTime =System.currentTimeMillis()
//        fileByRootElementMap.clear()
//        trans()
//        log("Complete!")
//        val endTime = System.currentTimeMillis()
//        log("耗时：${(endTime - startTime).formatTime()}")
//    }
//
//    private fun trans() {
//        //读取Excel内容转化为bean
//        val excelBook = HSSFWorkbook(File(parserConfig.excelPath).inputStream())
//        val sheet = excelBook.getSheet(parserConfig.projectName)
//            ?: throw ParserPluginException("Excel 中不存在sheet：${parserConfig.projectName}")
//        for (i in 0 until sheet.lastRowNum){
//            //跳过title
//            if (i > 1) {
//                writeCellToDoc(sheet.getRow(i))
//            }
//        }
//        writeDocToFile()
//    }
//
//    private fun writeDocToFile() {
//        fileByRootElementMap.forEach {
//            val fileOutputStream = FileOutputStream(it.key)
//            val format = OutputFormat()
//            format.encoding = "UTF-8"
//            format.isNewlines = true
//            format.indent = "    "
//            val xmlWriter = XMLWriter(fileOutputStream, format)
//            //设置是否转义。默认true，代表转义
//            xmlWriter.isEscapeText = false
//            xmlWriter.write(it.value)
//            xmlWriter.close()
//            fileOutputStream.close()
//        }
//    }
//
//    private fun writeCellToDoc(row: Row) {
//        val moduleName = row.getModuleName()
//        val keyName = row.getKeyName()
//        val resDir = File(parserConfig.projectPath, moduleName.plus(File.separator).plus("src/main/res"))
//        row.forEach { cell ->
//            when (cell.columnIndex) {
//                ParserConfig.indexModuleCell -> {
//                }
//                ParserConfig.indexKeyCell -> {
//                }
//                ParserConfig.indexDefCell -> {//values
//                    val dir = File(resDir, "values")
//                    if (!dir.exists()) dir.mkdirs()
//                    getDomHelper(dir).apply {
//                        val string = cell.richStringCellValue?.string?.stringChartFormat(keyName)
//                        addElement("string").apply {
//                            addText(string)
//                            addAttribute("name", keyName)
//                        }
//                    }
//                }
//                else -> {//values-xx
//                    val langName = parserConfig.langTypes[cell.columnIndex]
//                    val dir = File(resDir, "values-${langName}")
//                    if (!dir.exists()) dir.mkdirs()
//                    getDomHelper(dir).apply {
//                        val string = cell.richStringCellValue?.string?.stringChartFormat(langName)
//                        addElement("string").apply {
//                            addText(string)
//                            addAttribute("name", keyName)
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    private fun getDomHelper(dir: File): Element {
//        val xmlFile = File(dir, "strings.xml")
//        if (!xmlFile.exists()) xmlFile.createNewFile()
//        if (!fileByRootElementMap.containsKey(xmlFile)) {
//            val doc = DocumentHelper.createDocument()
//            doc.addElement("resources")
//            fileByRootElementMap[xmlFile] = doc
//        }
//        return fileByRootElementMap[xmlFile]!!.rootElement
//    }
//
//    private fun Row.getModuleName() =
//        getCell(parserConfig.langTypes.indexOf(ParserConfig.excelModuleRowName))!!.richStringCellValue.string
//
//    private fun Row.getKeyName() =
//        getCell(parserConfig.langTypes.indexOf(ParserConfig.excelKeyRowName))!!.richStringCellValue.string
//
//    //数组降维
//    private fun <T> List<List<T>>.dimensionReduction(): MutableList<T> {
//        val mutableListOf = mutableListOf<T>()
//        forEach { mutableListOf.addAll(it) }
//        return mutableListOf
//    }
//}