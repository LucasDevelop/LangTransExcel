package com.lucas.lang.v2.ui

import com.android.tools.adtui.validation.ErrorDetailDialog
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.DialogWrapper
import com.lucas.lang.ext.createCheckBoxListView
import com.lucas.lang.ext.labelSize
import com.lucas.lang.ui.ProgressLogDialog
import com.lucas.lang.v2.bean.SheetBean
import com.lucas.lang.v2.confoig.InputConfig
import com.lucas.lang.v2.ext.showError
import com.lucas.lang.v2.ext.showMessage
import com.lucas.lang.v2.ext.showWaning
import com.lucas.lang.v2.utils.ExcelUtil
import com.lucas.lang.v2.utils.XmlParserUtil
import java.awt.Component
import java.awt.Dimension
import java.io.File
import javax.swing.*

/**
 * 导入配置
 */
class InputConfigDialog(val anActionEvent: AnActionEvent, val inputConfig: InputConfig) : DialogWrapper(true) {
    companion object {
        fun show(anActionEvent: AnActionEvent, inputConfig: InputConfig) {
            InputConfigDialog(anActionEvent, inputConfig).apply {
                pack()
                showAndGet()
            }
        }
    }

    init {
        init()
        title = "导入设置"
        setSize(300, 300)
    }

    //    var rbs: List<JRadioButton>? = null
    private var excelPath: JTextField? = null
    private var rootBox: Box? = null
    private var sheetCombo: JComboBox<String>? = null
    private var defLangCombo: JComboBox<String>? = null
    private var modulesCombo: JComboBox<String>? = null
    private var progressLogDialog: ProgressLogDialog? = null
    private var langsJCheckBoxList: List<JCheckBox>? = null
    private var moduleJCheckBoxList: List<JCheckBox>? = null

    override fun createCenterPanel(): JComponent = Box.createVerticalBox().apply {
        rootBox = this
        add(createFilePathBox())
    }

    //其他配置
    private fun showOtherSettings() {
        rootBox?.add(Box.createHorizontalBox().also { parent ->
            parent.add(JLabel("是否自动补全缺失的文件夹和文件:").labelSize())
            parent.add(Box.createHorizontalGlue())
            parent.add(JCheckBox("启用/关闭").apply {
                isSelected = true
                addActionListener { inputConfig.isAutoCompletionDirOrFile = isSelected }
            })
        })
//        rootBox?.add(Box.createHorizontalBox().also { parent ->
//            parent.add(JLabel("是否自动补全缺失的key:").labelSize())
//            parent.add(Box.createHorizontalGlue())
//            parent.add(JCheckBox("启用/关闭").apply {
//                addActionListener { inputConfig.isAutoCompletionKey = isSelected }
//            })
//        })
        rootBox?.add(Box.createHorizontalBox().also { parent ->
            parent.add(JLabel("是否开启智能修复:").labelSize())
            parent.add(Box.createHorizontalGlue())
            parent.add(JCheckBox("启用/关闭").apply {
                isSelected = true
                addActionListener { inputConfig.isSmartFix = isSelected }
            })
        })
        rootBox?.revalidate()
    }


    //选择文件
    private fun createFilePathBox(): JComponent =
        Box.createHorizontalBox().apply {
            add(JLabel("Excel文件路径:").labelSize())
            add(Box.createHorizontalGlue())
            add(Box.createHorizontalBox().apply {
                excelPath = JTextField()
                excelPath?.isEnabled = false
                add(excelPath.apply { minimumSize = Dimension(300, 10) })
                add(Box.createHorizontalStrut(10))
                add(JButton("选择文件/保存路径").apply {
                    addActionListener {
                        //选择文件
                        val fileChooser = JFileChooser()
                        fileChooser.fileSelectionMode = JFileChooser.FILES_ONLY
                        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                            val selectedFile: File = fileChooser.selectedFile
                            if (selectedFile.exists()) {
                                excelPath!!.text = selectedFile.absolutePath
                                inputConfig.excelPath = selectedFile.absolutePath
                                parserExcel()
                            }
                        }
                    }
                })
            })
        }

    private fun createDefValueLang(langs: List<String>): Component? = Box.createHorizontalBox().also { box ->
        box.add(JLabel("选择默认语言语种:").labelSize())
        box.add(Box.createHorizontalGlue())
        defLangCombo = JComboBox<String>()
        defLangCombo?.addItem("请选择")
        langs.forEach {
            defLangCombo?.addItem(it)
        }
        box.add(defLangCombo)
    }

    private fun createSheetNamesBox(): Component = Box.createHorizontalBox().also { box ->
        box.add(JLabel("选择Excel页码:").labelSize())
        box.add(Box.createHorizontalGlue())
        sheetCombo = JComboBox<String>()
        sheetCombo?.addItem("请选择")
        inputConfig.sheetNames.forEach {
            sheetCombo?.addItem(it.sheetName)
        }
        box.add(sheetCombo)
    }

    //所有语言类型
    private fun createLangsBox(langs: List<String>): JComponent =
        Box.createHorizontalBox().also { parent ->
            parent.add(JLabel("选择需要导入的语言类型:").labelSize())
            parent.add(Box.createHorizontalGlue())
            langsJCheckBoxList = mutableListOf()
            val moduleListView = createCheckBoxListView(langs) { cb, text ->
                //如果excel中的module在项目中找不到则不可选择
                cb.isSelected = true
                langsJCheckBoxList = langsJCheckBoxList?.plus(cb)
            }
            parent.add(moduleListView)
        }

    //所有模块
    private fun createModulesBox(modules: List<String>): JComponent =
        Box.createHorizontalBox().also { parent ->
            parent.preferredSize = Dimension(200, 100)
            parent.add(JLabel("选择需要导入的模块:").labelSize())
            parent.add(Box.createHorizontalGlue())
            moduleJCheckBoxList = mutableListOf()
            val moduleListView = createCheckBoxListView(modules) { cb, text ->
                //如果excel中的module在项目中找不到则不可选择
                val isExists = inputConfig.allModuleBeans?.find { it.moduleName == text } != null
                cb.isSelected = isExists
                cb.isEnabled = isExists
                moduleJCheckBoxList = moduleJCheckBoxList?.plus(cb)
            }
            parent.add(moduleListView)
        }

    override fun doOKAction() {
        //获取用户选择的页码
        val sheetIndex = sheetCombo?.selectedIndex ?: 0
        if (!inputConfig.sheetNames.isNullOrEmpty() && inputConfig.sheetNames.size > 1 && sheetIndex == 0) {
            "请先选择一个页码!".showMessage()
            return
        }
        if (inputConfig.sheetNames.size > 1) {
            inputConfig.selectSheet = inputConfig.sheetNames[sheetIndex - 1]
        }
        if (inputConfig.rows.isNullOrEmpty()) {//还未解析sheet，或者未解析出内容
            parserData()
            return
        }
        //获取用户选择的module
        val selectModules = moduleJCheckBoxList?.filter { it.isSelected }?.map { it.text }
        inputConfig.selectModuleBeans =
            inputConfig.allModuleBeans?.filter { selectModules?.contains(it.moduleName) == true }
        if (inputConfig.allModuleBeans.isNullOrEmpty()) {
            "请先至少选择一个Module!".showWaning()
            return
        }
        //获取用户选择的语言类型
        inputConfig.selectLangs = langsJCheckBoxList?.filter { it.isSelected }?.map { it.text }
        if (inputConfig.selectLangs.isNullOrEmpty()) {
            "请先至少选择一个语言类型!".showWaning()
            return
        }
        //获取默认语言类型
        val defLangIndex = defLangCombo?.selectedIndex ?: 0
        if (defLangIndex == 0) {
            "请选择默认语言类型!".showMessage()
            return
        }
        inputConfig.defValueLangName = inputConfig.selectLangs?.get(defLangIndex - 1)

        super.doOKAction()
        //开始写入xml
        startWrite()
    }

    private fun startWrite() {
        val progressLog = ProgressLogDialog.getInstance("导入详情")
        Thread {
            progressLog.addLog("开始写入xml。。。")
            val startTime = System.currentTimeMillis()
            try {
                XmlParserUtil.writeRowBeanByXml(inputConfig, {
                    progressLog.addLog(it)
                }, {
                    progressLog.addLog(it)
                }, {
                    progressLog.addLog("耗时：${(System.currentTimeMillis() - startTime) / 1000}s")
                })
            }catch (e:Exception){
                progressLog.addLog("写入异常：${e.message}")
            }

        }.start()
        progressLog.isVisible = true
    }


    private fun parserExcel() {
        val file = File(inputConfig.excelPath)
        if (!file.exists()) {
            "Excel文件不存在!".showError()
            return
        }
        if (progressLogDialog == null)
            progressLogDialog = ProgressLogDialog.getInstance("解析Excel数据")
        Thread {
            ExcelUtil.getSheetList(inputConfig.excelPath)?.map { SheetBean(it.sheetNo, it.sheetName) }?.apply {
                forEach {
                    progressLogDialog?.addLog("发现：$it")
                }
                inputConfig.sheetNames.addAll(this)
                SwingUtilities.invokeLater {
                    progressLogDialog?.isVisible = false
                    parserSheet(inputConfig)
                }
            }
        }.start()
        progressLogDialog?.addLog("开始解析Excel表格数据")
        progressLogDialog?.isVisible = true
        return
    }

    //解析sheet数据
    private fun parserSheet(inputConfig: InputConfig) {
        if (inputConfig.sheetNames.isNullOrEmpty()) {//excel表格是空的
            "Excel无内容.".showError()
            return
        } else if (inputConfig.sheetNames.size == 1) {//默认选择第一个
            inputConfig.selectSheet = inputConfig.sheetNames.first
            parserData()
        } else {//让用户选择sheet
            rootBox?.add(createSheetNamesBox())
            rootBox?.revalidate()
        }
    }

    private fun parserData() {
        inputConfig.selectSheet?.also { selectSheet ->
            if (progressLogDialog == null)
                progressLogDialog = ProgressLogDialog.getInstance("解析\"${selectSheet.sheetName}\"数据")
            Thread {
                val startTime = System.currentTimeMillis()
                ExcelUtil.readRowsBySheetName(inputConfig, selectSheet.sheetIndex, {
                    progressLogDialog?.addLog(it.toString())
                }, { errorMsg ->
                    progressLogDialog?.addLog("错误：$errorMsg")
                }, { rows ->
                    inputConfig.rows = rows
                    //读取语言类型
                    if (inputConfig.allLangs.isNullOrEmpty()) {
                        progressLogDialog?.addLog("错误：未读取到语言类型")
                    } else {
                        SwingUtilities.invokeLater {
                            rootBox?.add(createLangsBox(inputConfig.allLangs!!))
                            rootBox?.add(createDefValueLang(inputConfig.allLangs!!))
                            showOtherSettings()
                            rootBox?.revalidate()
                        }
                    }
                    //读取property字段，识别出module有哪些，并与项目中的module取交集
                    val moduleNames = rows.groupBy { it.rowAttr.moduleName }.map { it.key!! }
                    SwingUtilities.invokeLater {
                        rootBox?.add(createModulesBox(moduleNames))
                        rootBox?.revalidate()
                    }
                    progressLogDialog?.addLog("Excel解析完成!")
                    progressLogDialog?.addLog("耗时：${(System.currentTimeMillis() - startTime) / 1000}s")
                    SwingUtilities.invokeLater { progressLogDialog?.isVisible = false }
                })
            }.start()
            if (progressLogDialog?.isVisible == false)
                progressLogDialog?.isVisible = true
        }
    }
}