package com.lucas.lang.v2.utils

import com.lucas.lang.ext.stringChartFormat
import com.lucas.lang.v2.bean.ModuleBean
import com.lucas.lang.v2.bean.RowAttr
import com.lucas.lang.v2.bean.RowBean
import com.lucas.lang.v2.bean.XmlFileBean
import com.lucas.lang.v2.confoig.Constant
import com.lucas.lang.v2.confoig.InputConfig
import com.lucas.lang.v2.ext.ifExistsMakes
import org.dom4j.CDATA
import org.dom4j.Document
import org.dom4j.DocumentHelper
import org.dom4j.Element
import org.dom4j.io.OutputFormat
import org.dom4j.io.SAXReader
import org.dom4j.io.XMLWriter
import java.io.File
import java.io.FileOutputStream

/**
 * xml解析
 */
object XmlParserUtil {

    //将RowBean写入xml
    fun writeRowBeanByXml(
        inputConfig: InputConfig,
        progress: (msg: String) -> Unit,
        error: (msg: String) -> Unit,
        complete: () -> Unit
    ) {
        if (inputConfig.rows.isNullOrEmpty() || inputConfig.selectModuleBeans.isNullOrEmpty() || inputConfig.selectLangs.isNullOrEmpty()) {
            error("参数配置错误.")
            return
        }
        //将rowBean按照module进行分类，提高写入速度
        inputConfig.rows!!.groupBy { row ->
            inputConfig.selectModuleBeans?.find { row.rowAttr.moduleName == it.moduleName }
        }.filter { it.key != null }.forEach { (moduleBean, rows) ->
            //加入默认语种
            val allLangRows = moduleBean?.xmlFiles?.toMutableList()
            val defLang = XmlFileBean()
            defLang.filePath = File(moduleBean!!.moduleFilePath, "src/main/res/values/strings.xml").ifExistsMakes().absolutePath
            defLang.langName = Constant.DEF_LANG
            allLangRows?.add(defLang)
            //将每个module的row按照语言类型写入
            allLangRows?.forEach { xmlFileBean ->
                if (!inputConfig.selectLangs!!.contains(xmlFileBean.langName) && xmlFileBean.langName != Constant.DEF_LANG) {
                    progress("跳过语种：${xmlFileBean.langName}")
                    return@forEach
                }
                val xmlFile = File(xmlFileBean.filePath).ifExistsMakes()
                var doc: Document?
                if (!xmlFile.exists()) {//新增文件
                    if (inputConfig.isAutoCompletionDirOrFile) {
                        xmlFile.createNewFile()
                        progress("创建新文件：${xmlFileBean.filePath}")
                        doc = DocumentHelper.createDocument()
                        val rootElement = DocumentHelper.createElement("resources")
                        doc.rootElement = rootElement
                        addRowToDoc(rows, xmlFileBean, rootElement, progress, inputConfig, doc)
                    }
                }
                if (!xmlFile.exists()) {
                    error("文件未找到：${xmlFileBean.filePath}")
                    return@forEach
                } else {//新增\修改字段
                    doc = SAXReader().read(xmlFile)
                    val rootElement = doc.rootElement
                    addRowToDoc(rows, xmlFileBean, rootElement, progress, inputConfig, doc)
                }
                writeDocToFile(doc, xmlFile)
                progress("写入完成：${xmlFileBean.filePath}")
            }
        }
        complete()
    }

    private fun addRowToDoc(
        rows: List<RowBean>,
        xmlFileBean: XmlFileBean,
        rootElement: Element,
        progress: (msg: String) -> Unit,
        inputConfig: InputConfig,
        doc: Document
    ) {
        rows.forEach { row ->
            var lang = xmlFileBean.langName
            if (lang == Constant.DEF_LANG && !inputConfig.defValueLangName.isNullOrBlank()) {//设置默认语种
                lang = inputConfig.defValueLangName!!
            }
            if (row.langs.containsKey(lang)) {
                //判断字段是否已存在
                val value = row.langs[lang]
                val insertValue = if (inputConfig.isSmartFix) value?.stringChartFormat(lang) else value
                val selectNodes = doc.selectNodes("/resources/string[@name='${row.rowAttr.keyName}']")
                if (!selectNodes.isNullOrEmpty()) {//重复字段直接覆盖内容
                    selectNodes.first().text = insertValue
                } else {//新增字段
                    val element = DocumentHelper.createElement("string")
                    element.addAttribute("name", row.rowAttr.keyName)
                    element.text = insertValue
                    rootElement.add(element)
                }
                progress("写入字段:${row.rowAttr.keyName}->$value")
            }
        }
    }

    private fun writeDocToFile(document: Document, file: File) {
        val fileOutputStream = FileOutputStream(file)
        val format = OutputFormat()
        format.encoding = "UTF-8"
        format.isNewlines = true
        format.indent = "\t"
        val xmlWriter = XMLWriter(fileOutputStream, format)
        //设置是否转义。默认true，代表转义
        xmlWriter.isEscapeText = false
        xmlWriter.write(document)
        xmlWriter.flush()
        xmlWriter.close()
    }

    //将xml转化未RowBean
    fun parseXmlByModule(modules: List<ModuleBean>, block: (RowBean) -> Unit): HashMap<ModuleBean, ArrayList<RowBean>> {
        val hashMap = HashMap<ModuleBean, ArrayList<RowBean>>()
        modules.forEach { module ->
            val rows = ArrayList<RowBean>()
            module.xmlFiles.forEach { file ->
                if (File(file.filePath).exists()) {
                    val elementIterator = SAXReader().read(file.filePath).rootElement.elementIterator()
                    while (elementIterator.hasNext()) {
                        val element = elementIterator.next()
                        val isCdata = element.content().any { it is CDATA }
                        var cellValue = element.text
                        val key = element.attribute("name").value
                        if (isCdata) cellValue = "<![CDATA[$cellValue]]>"
                        val row = rows.find { it.rowAttr.keyName == key } ?: RowBean().apply {
                            if (rowAttr == null) {
                                rowAttr = RowAttr()
                            }
                            rowAttr.keyName = key
                            rowAttr.moduleName = module.moduleName
                            syncRowAttr()
                            rows.add(this)
                        }
                        row.langs[file.langName] = cellValue
//                        row.setLangValue(file.langName, cellValue)
                        block(row)
                    }
                }
            }
            hashMap[module] = rows
        }
        return hashMap
    }


}