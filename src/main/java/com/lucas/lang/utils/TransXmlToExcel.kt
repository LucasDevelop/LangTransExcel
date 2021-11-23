package com.lucas.lang.utils

import com.lucas.lang.bean.*
import com.lucas.lang.exception.ParserPluginException
import com.lucas.lang.ext.*
import org.apache.poi.hssf.usermodel.*
import org.apache.poi.hssf.util.HSSFColor
import org.apache.poi.ss.usermodel.Font
import org.apache.poi.ss.util.CellRangeAddress
import org.dom4j.CDATA
import org.dom4j.io.SAXReader
import java.io.File
import java.io.FileOutputStream
import java.util.regex.Pattern

/**
 * File TransformUtil.kt
 * Date 2021/8/26
 * Author lucas
 * Introduction xml 导出 excel
 */
object TransXmlToExcel {
    private lateinit var parserConfig: ParserConfig

    fun initConfig(parserConfig: ParserConfig): TransXmlToExcel {
        TransXmlToExcel.parserConfig = parserConfig
        parserConfig.langTypes = ArrayList(parserConfig.orgLangTypes)
        //检查参数
        if (parserConfig.isEnableOnlineTranslation && !parserConfig.orgLangTypes.contains(parserConfig.onlineByLangType))
            throw ParserPluginException("onlineByLangType类型必须属于orgLangTypes中的一员！")
        if (parserConfig.projectName.isEmpty()) throw ParserPluginException("projectName不能为空！")
        if (parserConfig.projectPath.isEmpty()) throw ParserPluginException("projectPath不能为空！")
        if (!File(parserConfig.projectPath).exists()) throw ParserPluginException("项目地址：${parserConfig.projectPath}不存在！")
        if (!File(parserConfig.excelPath).exists()) throw ParserPluginException("请选择Excel地址！${parserConfig.excelPath}")
        if (File(parserConfig.excelPath).isDirectory) {
            parserConfig.excelPath = parserConfig.excelPath + File.separator + ParserConfig.defExcelName
        }
        return this
    }

    fun start() {
        val startTime = System.currentTimeMillis()
        parserConfig.langTypes.add(ParserConfig.indexModuleCell, ParserConfig.excelModuleRowName)
        parserConfig.langTypes.add(ParserConfig.indexKeyCell, ParserConfig.excelKeyRowName)
        parserConfig.langTypes.add(ParserConfig.indexDefCell, ParserConfig.excelDefRowName)
        parserConfig.langTypes.add(parserConfig.langTypes.size, ParserConfig.excelRemarkRowName)
        trans(File(parserConfig.projectPath), File(parserConfig.excelPath))
        release()
        log("Complete!")
        val endTime = System.currentTimeMillis()
        log("耗时：${(endTime - startTime).formatTime()}")
    }

    private fun trans(projectPathFile: File, excelFile: File) {
        val pair = getExcelBook(excelFile)
        val excelBook: HSSFWorkbook = pair.first
//        var isNewExcelFile = pair.second
        val xmlFiles = scanProjectFiles(projectPathFile)
        val langSheet = getLangSheet(excelBook)
//        val titleRow = langSheet.getRow(0)//标题行
        //解析xml数据
        val moduleElements = parseXmlFile(xmlFiles)
        if (parserConfig.isEnableOnlineTranslation) {
            onlineTranslation(moduleElements)
        }
        //写入excel
        moduleElements.forEach { moduleBean ->
            val isExitModuleCell = langSheet.filter { it.rowNum >= 1 }.any {
                val moduleName = it.getCell(ParserConfig.indexModuleCell)?.richStringCellValue?.string
                moduleName == moduleBean.moduleName
            }
            moduleBean.rowBean.forEach { rowElement ->
                var row = langSheet.filter { it.rowNum >= 1 }.find {
                    val moduleName = it.getCell(ParserConfig.indexModuleCell)?.richStringCellValue?.string
                    val keyName = it.getCell(ParserConfig.indexKeyCell)?.richStringCellValue?.string
                    moduleName == moduleBean.moduleName && keyName == rowElement.key
                } as? HSSFRow
                val isExitKeyCell = row != null
                //add module\key cell
                if (row == null) {//新增row
                    row = langSheet.createRow(langSheet.count())
                    if (!isExitModuleCell) {//add module cell
                        setNormalCellValues(
                            row,
                            excelBook,
                            ElementStatus.NEW,
                            ParserConfig.excelModuleRowName,
                            moduleBean.moduleName
                        )
                    }
                    if (!isExitKeyCell) {//add key cell
                        setNormalCellValues(
                            row,
                            excelBook,
                            ElementStatus.NEW,
                            ParserConfig.excelKeyRowName,
                            rowElement.key
                        )
                    }
                } else {//update module and key by cell status
                    setNormalCellValues(
                        row,
                        excelBook,
                        ElementStatus.NORMAL,
                        ParserConfig.excelModuleRowName,
                        moduleBean.moduleName
                    )
                    setNormalCellValues(
                        row,
                        excelBook,
                        ElementStatus.NORMAL,
                        ParserConfig.excelKeyRowName,
                        rowElement.key
                    )
                }
                //add values cell
                rowElement.langElements.forEach { langElement ->
                    if (!isExitKeyCell) {//add new cell
                        setNormalCellValues(
                            row!!,
                            excelBook,
                            ElementStatus.NEW,
                            langElement.rowName,
                            langElement.cellValue
                        )
                    } else {
                        val oldCellValue =
                            row!!.getCell(parserConfig.langTypes.indexOf(langElement.rowName))?.richStringCellValue?.string
                        if (!oldCellValue.isNullOrEmpty() && oldCellValue != langElement.cellValue) {//覆盖新的值
                            setNormalCellValues(
                                row,
                                excelBook,
                                ElementStatus.UPDATE,
                                langElement.rowName,
                                langElement.cellValue
                            )
                        } else {
                            //update old cell status
                            setNormalCellValues(
                                row,
                                excelBook,
                                ElementStatus.NORMAL,
                                langElement.rowName,
                                langElement.cellValue
                            )
                        }
                    }
                }
            }
        }
        excelBook.write(FileOutputStream(excelFile))
    }

    //对数据进行在线翻译
    private fun onlineTranslation(moduleElements: List<ModuleBean>) {
        moduleElements.forEach {
            it.rowBean.forEach { rowElement ->
                //查找缺失的语言
                val map = rowElement.langElements.map { it.rowName }
                val missType = parserConfig.orgLangTypes.subtract(map)
                //开始进行翻译--基于某个语言类型
                val templateLang = rowElement.langElements.find { it.rowName == parserConfig.onlineByLangType }
                if (templateLang != null) {
                    missType.forEach { newType ->
                        val text = templateLang.cellValue
                        val from = parserConfig.onlineByLangType
                        val to = newType
                        OnlineTranslationHelper.transLang(text, from, to).also { onlineBean ->
                            if (onlineBean.isSuccess && !onlineBean.trans_result.isNullOrEmpty()) {
                                val element =
                                    LangElement(newType, onlineBean.trans_result.first().dst, ElementStatus.NEW)
                                rowElement.langElements.add(element)
                            }
                        }
                    }
                } else {
                    log("跳过翻译，原因：module->${rowElement.moduleName},key->${rowElement.key}无缺少${parserConfig.onlineByLangType}类型资源")
                }
            }
        }
    }

    //解析xml文件
    private fun parseXmlFile(xmlFiles: MutableList<XmlFileBean>) =
        xmlFiles.groupBy { it.moduleName }.map { modulefiles ->
            val elementByModule = mutableListOf<ElementPlus>()
            //解析module下的xml
            modulefiles.value.forEach { xmlFileBean ->
                elementByModule.addAll(
                    SAXReader().read(xmlFileBean.file).rootElement.elements()
                        .map { ElementPlus(xmlFileBean.langType, it) })
            }
            //将module下的element根据key进行分组
            elementByModule.groupBy { it.element.attribute("name").value }.map { elementPlus ->
                elementPlus.value.map {
                    val isCdata = it.element.content().any { it is CDATA }
                    var cellValue = it.element.text
                    if (isCdata) cellValue = "<![CDATA[$cellValue]]>"
                    LangElement(
                        it.langType,
                        cellValue,
                    )
                }.let {
                    RowElement(elementPlus.key, it.toMutableList(), modulefiles.key)
                }
            }.let { ModuleBean(modulefiles.key, it) }
        }

    private fun getExcelBook(excelFile: File): Pair<HSSFWorkbook, Boolean> {
        val excelBook: HSSFWorkbook
        var isNewExcelFile = false
        if (!excelFile.exists()) {//如果excel不存在则创建新表
            logDividerStartEnd("创建Excel文件")
            excelBook = HSSFWorkbook()
            excelFile.createNewFile()
            isNewExcelFile = true
        } else {
            excelBook = HSSFWorkbook(excelFile.inputStream())
        }
        return Pair(excelBook, isNewExcelFile)
    }

    //扫描项目中的资源
    private fun scanProjectFiles(projectPathFile: File): MutableList<XmlFileBean> {
        val xmlFiles = mutableListOf<File>()
        FileUtil.findAllFile(projectPathFile, xmlFiles) {
            //匹配文件规则
            val parentFile = it.parentFile
            val matches = Pattern.compile(parserConfig.dirPattern).matcher(parentFile.name).find()
            return@findAllFile parentFile != null &&
                    parentFile.isDirectory &&
                    matches &&
                    it.name.equals("strings.xml")
        }
        logDividerStart("扫描项目xml文件")
        xmlFiles.forEach { log("${if (it.isFile) "File" else "Dir"}:${it.name}->${it.absolutePath}") }
        logDividerEnd("扫描项目xml文件")
        return xmlFiles.map {
            val dirName = it.parentFile.name
            val moduleName = it.parentFile.parentFile.parentFile.parentFile.parentFile.name
            val langType =
                if (!dirName.contains("-")) ParserConfig.excelDefRowName else dirName.substring(dirName.indexOf("-") + 1)
            if (!parserConfig.langTypes.contains(langType)){
                log("ParserConfig.langTypes中缺少\"$langType\"语言类型")
                return@map null
//                throw ParserPluginException("ParserConfig.langTypes中缺少\"$langType\"语言类型")
            }
            XmlFileBean(it, moduleName, langType)
        }.filterNotNull().toMutableList()
    }

    private fun getLangSheet(excelBook: HSSFWorkbook): HSSFSheet {
        var sheet = excelBook.getSheet(parserConfig.projectName)
        if (sheet == null) {
            sheet = excelBook.createSheet(parserConfig.projectName)
            sheet.createRow(0)
            sheet.createRow(1)
            logDividerStartEnd("新建sheet:${parserConfig.projectName}")
        }
        addRemark(sheet, excelBook)
        //检查语言列数是否缺失，否则补齐
        val titleStyle = createTitleStyle(excelBook)
        val titleRow = sheet.getRow(1)
        parserConfig.langTypes.forEach { langName ->
            if (titleRow.find { it.richStringCellValue.string == langName } == null) {//补齐
                val cell = titleRow.createCell(parserConfig.langTypes.indexOf(langName))
                cell.setCellStyle(titleStyle)
                cell.setCellValue(HSSFRichTextString(langName))
                logDividerStartEnd("sheet:${parserConfig.projectName} 创建标题->${langName}")
            }
        }
        //设置单元格宽度
        parserConfig.langTypes.forEachIndexed { index, s ->
            sheet.setColumnWidth(index, if (index == 0) 30 * 256 else 50 * 256)
        }
        return sheet
    }

    private fun addRemark(
        sheet: HSSFSheet,
        excelBook: HSSFWorkbook
    ) {
        //显示备注信息
        val remarkRow = sheet.getRow(0)
        remarkRow.height = 200 * 20
        var remarkCell = remarkRow.getCell(0)
        if (remarkCell == null) {
            remarkCell = remarkRow.createCell(0)
            //合并单元格
            sheet.addMergedRegion(CellRangeAddress(0, 0, 0, 2))
        }
        remarkCell.setCellStyle(excelBook.createCellStyle().apply {
            wrapText = true
            setFont(excelBook.createFont().apply {
                //标题放大加粗
                color = HSSFColor.RED.index
                boldweight = Font.BOLDWEIGHT_BOLD
            })
        })
        remarkCell.setCellValue(HSSFRichTextString(ParserConfig.remark))
    }

    private var normalCellStyle: HSSFCellStyle? = null
    private var newCellStyle: HSSFCellStyle? = null
    private var updateCellStyle: HSSFCellStyle? = null
    private var repeatCellStyle: HSSFCellStyle? = null
    private var errorCellStyle: HSSFCellStyle? = null

    private fun getCellStyleByStatus(excelBook: HSSFWorkbook, status: ElementStatus): HSSFCellStyle {
        return when (status) {
            ElementStatus.NORMAL -> {//不做处理
                if (normalCellStyle == null) {
                    normalCellStyle = excelBook.createCellStyle().apply {
                        wrapText = true
                        fillPattern = HSSFCellStyle.SOLID_FOREGROUND
                        verticalAlignment = HSSFCellStyle.VERTICAL_CENTER
                        fillForegroundColor = HSSFColor.WHITE.index
                    }
                }
                normalCellStyle!!
            }
            ElementStatus.NEW -> {//新增
                if (newCellStyle == null) {
                    newCellStyle = excelBook.createCellStyle().apply {
                        wrapText = true
                        fillPattern = HSSFCellStyle.SOLID_FOREGROUND
                        verticalAlignment = HSSFCellStyle.VERTICAL_CENTER
                        fillForegroundColor = HSSFColor.SEA_GREEN.index
                    }
                }
                newCellStyle!!
            }
            ElementStatus.UPDATE -> {//覆盖
                if (updateCellStyle == null) {
                    updateCellStyle = excelBook.createCellStyle().apply {
                        wrapText = true
                        fillPattern = HSSFCellStyle.SOLID_FOREGROUND
                        verticalAlignment = HSSFCellStyle.VERTICAL_CENTER
                        fillForegroundColor = HSSFColor.YELLOW.index
                    }
                }
                updateCellStyle!!
//                style.setFont(excelBook.createFont().also {
//                    it.color = HSSFColor.SKY_BLUE.index
//                })
            }
            ElementStatus.REPEAT -> {//values key重复
                if (repeatCellStyle == null) {
                    repeatCellStyle = excelBook.createCellStyle().apply {
                        wrapText = true
                        fillPattern = HSSFCellStyle.SOLID_FOREGROUND
                        verticalAlignment = HSSFCellStyle.VERTICAL_CENTER
                        fillForegroundColor = HSSFColor.SKY_BLUE.index
                    }
                }
                repeatCellStyle!!
            }
            ElementStatus.ERROR -> {//异常
                if (errorCellStyle == null) {
                    errorCellStyle = excelBook.createCellStyle().apply {
                        wrapText = true
                        fillPattern = HSSFCellStyle.SOLID_FOREGROUND
                        verticalAlignment = HSSFCellStyle.VERTICAL_CENTER
                        fillForegroundColor = HSSFColor.RED.index
                    }
                }
                errorCellStyle!!
            }
        }
    }

    private fun setNormalCellValues(
        row: HSSFRow,
        excelBook: HSSFWorkbook,
        status: ElementStatus,
        rowName: String,
        cellValue: String
    ) {
        val currentCellIndex = parserConfig.langTypes.indexOf(rowName)
        var currentCell = row.getCell(currentCellIndex)
        when (status) {
            ElementStatus.NORMAL -> {//不做处理
                log("不做处理:${cellValue}")
            }
            ElementStatus.NEW -> {//新增
                currentCell = row.createCell(currentCellIndex).apply {
                    setCellValue(HSSFRichTextString(cellValue))
                }
                log("新增字段:${cellValue}")
            }
            ElementStatus.UPDATE -> {//覆盖
                if (parserConfig.isCoverLangValues)
                    currentCell?.setCellValue(HSSFRichTextString(cellValue))
                log("覆盖字段:${cellValue}")
            }
            ElementStatus.REPEAT -> {//values key重复
                log("重复字段:${cellValue}")
            }
            ElementStatus.ERROR -> {//异常
                log("异常字段:${cellValue}")
            }
        }
        currentCell?.setCellStyle(getCellStyleByStatus(excelBook, status))
    }

    //新建标题样式
    private fun createTitleStyle(excelBook: HSSFWorkbook): HSSFCellStyle? {
        val style = excelBook.createCellStyle()
        style.alignment = HSSFCellStyle.ALIGN_CENTER
        style.fillPattern = HSSFCellStyle.SOLID_FOREGROUND
        style.fillBackgroundColor = HSSFColor.DARK_RED.index
        style.setFont(excelBook.createFont().also {
            //标题放大加粗
            it.color = HSSFColor.BLACK.index
            it.boldweight = Font.BOLDWEIGHT_BOLD
        })
        return style
    }

    private fun release(){
        normalCellStyle = null
        newCellStyle = null
        updateCellStyle = null
        repeatCellStyle = null
        errorCellStyle = null
    }
}