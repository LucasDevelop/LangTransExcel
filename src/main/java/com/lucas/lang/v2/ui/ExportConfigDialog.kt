package com.lucas.lang.v2.ui

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.DialogWrapper
import com.lucas.lang.ext.createCheckBoxListView
import com.lucas.lang.ext.labelSize
import com.lucas.lang.ui.InitConfig
import com.lucas.lang.ui.ProgressLogDialog
import com.lucas.lang.v2.bean.RowBean
import com.lucas.lang.v2.confoig.Constant
import com.lucas.lang.v2.confoig.ExportConfig
import com.lucas.lang.v2.ext.showError
import com.lucas.lang.v2.ext.showMessage
import com.lucas.lang.v2.utils.ExcelUtil
import com.lucas.lang.v2.utils.XmlParserUtil
import java.awt.Dimension
import java.io.File
import javax.swing.*

class ExportConfigDialog(val exportConfig: ExportConfig, val anActionEvent: AnActionEvent) : DialogWrapper(true) {
    companion object {
        fun show(exportConfig: ExportConfig, anActionEvent: AnActionEvent) {
            ExportConfigDialog(exportConfig, anActionEvent).apply {
                pack()
                showAndGet()
            }
        }
    }

    init {
        init()
        title = "参数配置"
        setSize(300, 300)
    }

    private var exportRB: JRadioButton? = null
    private var importRB: JRadioButton? = null
    private var onlineLangTypeCombo: JComboBox<String>? = null
    private var excelPath: JTextField? = null
    private var moduleJCheckBoxList: List<JCheckBox>? = null
    private var langsJCheckBoxList: List<JCheckBox>? = null
    private var rootBox: Box? = null

    override fun createCenterPanel(): JComponent =
        Box.createVerticalBox().apply {
            rootBox = this
            rootBox?.refreshViews()
        }

    private fun Box.refreshViews() {
        removeAll()
//        add(createButtonsBox())
//        add(Box.createVerticalStrut(10))
//        add(createModelTypeBox())
//        add(Box.createVerticalStrut(10))
        add(createFilePathBox())
        add(Box.createVerticalStrut(10))
        add(createModulesBox(exportConfig.allModuleBeans?.map { it.moduleName } ?: mutableListOf()))
        add(Box.createVerticalStrut(10))
        val langs = HashSet<String>()
        exportConfig.allModuleBeans?.forEach {
            it.xmlFiles.forEach {
                langs.add(it.langName)
            }
        }
        exportConfig.allLangs = langs.toList()
        add(createLangsBox())
        add(Box.createVerticalStrut(10))
        add(createOnlineTransBox())
        add(Box.createVerticalStrut(10))
        add(createOnlineLangTempType())
    }

    private fun createButtonsBox(): JComponent =
        Box.createHorizontalBox().apply {
            val jButton = JButton("重新扫描项目")
            add(jButton)
            jButton.addActionListener {
                InitConfig.scanProject(anActionEvent) {
                    rootBox?.refreshViews()
                }
            }
        }

    //是否启用在线翻译
    private fun createOnlineTransBox(): JComponent =
        Box.createHorizontalBox().also { parent ->
            parent.add(JLabel("在线翻译:").labelSize())
            parent.add(Box.createHorizontalGlue())
            val jCheckBox = JCheckBox("启用/关闭")
            jCheckBox.addActionListener {
                exportConfig.isEnableOnlineTrans = jCheckBox.isSelected
                onlineLangTypeCombo?.isEnabled = jCheckBox.isSelected
            }
            parent.add(jCheckBox)
        }

    //指定在线翻译模版语言类型
    private fun createOnlineLangTempType() = Box.createHorizontalBox().also { box ->
        box.add(JLabel("选择在线翻译基于哪种语言:").labelSize())
        box.add(Box.createHorizontalGlue())
        onlineLangTypeCombo = JComboBox<String>()
        onlineLangTypeCombo?.isEnabled = false
        onlineLangTypeCombo?.addItem("请选择")
        exportConfig.allLangs?.forEach {
            onlineLangTypeCombo?.addItem(it)
        }
        box.add(onlineLangTypeCombo)
    }

    //所有语言类型
    private fun createLangsBox(): JComponent =
        Box.createHorizontalBox().also { parent ->
            parent.add(JLabel("选择语言类型:").labelSize())
            parent.add(Box.createHorizontalGlue())
            langsJCheckBoxList = mutableListOf()
            val moduleListView = createCheckBoxListView(exportConfig.allLangs!!) { cb, text ->
                //如果excel中的module在项目中找不到则不可选择
                cb.isSelected = true
                langsJCheckBoxList = langsJCheckBoxList?.plus(cb)
            }
            parent.add(moduleListView)
        }

    //所有模块
    private fun createModulesBox(modules: List<String>): JComponent =
        Box.createHorizontalBox().also { parent ->
            parent.add(JLabel("选择模块:").labelSize())
            parent.add(Box.createHorizontalGlue())
            moduleJCheckBoxList = mutableListOf()
            val moduleListView = createCheckBoxListView(modules) { cb, text ->
                cb.isSelected = true
                moduleJCheckBoxList = moduleJCheckBoxList?.plus(cb)
            }
            parent.add(moduleListView)
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
                add(JButton("选择文件").apply {
                    addActionListener {
                        //选择文件
                        val fileChooser = JFileChooser()
                        fileChooser.fileSelectionMode = JFileChooser.FILES_AND_DIRECTORIES
                        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                            val selectedFile: File = fileChooser.selectedFile
                            if (selectedFile.exists()) {
                                excelPath!!.text = selectedFile.absolutePath
                            }
                        }
                    }
                })
            })
        }

    override fun doOKAction() {
        val excel = excelPath?.text ?: ""
        val file = File(excel)
        if (!file.exists()) {
            "Excel文件地址不存在！".showError()
            return
        }
        exportConfig.excelPath = excel
        if (file.isDirectory) {
            exportConfig.excelPath = File(file, "国际化翻译.xlsx").absolutePath
        }
        val moduleNames = moduleJCheckBoxList!!.filter { it.isSelected }.map { it.text }
        if (moduleNames.isEmpty()) {
            "请至少选择一个module！".showMessage()
            return
        }
        val langNames = langsJCheckBoxList!!.filter { it.isSelected }.map { it.text }
        if (langNames.isEmpty()) {
            "请至少选择一个语言类型！".showMessage()
            return
        }
        if (exportConfig.isEnableOnlineTrans) {
            val onlineIndex = onlineLangTypeCombo?.selectedIndex ?: 0
            if (onlineIndex == 0) {
                "选择在线翻译基于哪种语言！".showMessage()
                return
            }
            exportConfig.onlineTransLangType = exportConfig.allLangs?.get(onlineIndex - 1)
            if (!Constant.baiduLangTypeMap.containsKey(exportConfig.onlineTransLangType)) {
                "在线翻译暂时不支持【${exportConfig.onlineTransLangType}】类型".showError()
                return
            }
        }
        super.doOKAction()
        //更新配置缓存
        exportConfig.selectModuleBeans = exportConfig.allModuleBeans?.filter { moduleNames.contains(it.moduleName) }
        exportConfig.selectLangs = langNames
        runTask()
    }

    private fun runTask() {
        if (!exportConfig.selectModuleBeans.isNullOrEmpty()) {
            val progressLogDialog = ProgressLogDialog.getInstance("扫描项目")
            val startTime = System.currentTimeMillis()
            Thread {
                progressLogDialog.addLog("开始导出数据")
                XmlParserUtil.parseXmlByModule(exportConfig.selectModuleBeans!!) {
                    progressLogDialog.addLog("导出字段:${it}")
                }.also {
                    progressLogDialog.addLog("开始写入Excel")
                    val list = ArrayList<RowBean>()
                    it.forEach {
                        list.addAll(it.value)
                    }
                    ExcelUtil.writeDataBySheetName(exportConfig.excelPath, exportConfig.projectName, exportConfig, list)
                }
                val endTime = System.currentTimeMillis()
                progressLogDialog.addLog("导出数据完成,耗时：${(endTime - startTime) / 1000}s")
            }.start()
            progressLogDialog.isVisible = true
        }
    }

}