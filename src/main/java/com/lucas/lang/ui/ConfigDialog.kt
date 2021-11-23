package com.lucas.lang.ui

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.lucas.lang.ext.labelSize
import com.lucas.lang.utils.ParserConfig
import java.awt.Dimension
import java.io.File
import javax.swing.*

class ConfigDialog(val parserConfig: ParserConfig,val anActionEvent: AnActionEvent) : DialogWrapper(true) {
    companion object {
        fun show(parserConfig: ParserConfig,anActionEvent : AnActionEvent) {
            ConfigDialog(parserConfig,anActionEvent).apply {
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
    private var excelPath: JTextField? = null
    private var moduleJCheckBoxList: List<JCheckBox> = arrayListOf()
    private var langsJCheckBoxList: List<JCheckBox> = arrayListOf()
    private var rootBox:Box?=null

    override fun createCenterPanel(): JComponent =
        Box.createVerticalBox().apply {
            rootBox = this
            rootBox?.refreshViews()
        }

    private fun Box.refreshViews() {
        removeAll()
        add(createButtonsBox())
        add(Box.createVerticalStrut(10))
        add(createModelTypeBox())
        add(Box.createVerticalStrut(10))
        add(createFilePathBox())
        add(Box.createVerticalStrut(10))
        add(createModulesBox(parserConfig.moduleDir.map { it.name }))
        add(Box.createVerticalStrut(10))
        add(createLangsBox(parserConfig.langTypes))
        add(Box.createVerticalStrut(10))
        add(createOnlineTransBox())
    }

    private fun createButtonsBox(): JComponent=
        Box.createHorizontalBox().apply {
            val jButton = JButton("重新扫描项目")
            add(jButton)
            jButton.addActionListener {
                InitConfig.scanProject(anActionEvent){
                    rootBox?.refreshViews()
                }
            }
        }

    //是否启用在线翻译
    private fun createOnlineTransBox(): JComponent =
        Box.createHorizontalBox().also { parent ->
            parent.add(JLabel("在线翻译:").labelSize())
            parent.add(Box.createHorizontalGlue())
            parent.add(JCheckBox("启用/关闭"))
        }

    //所有语言类型
    private fun createLangsBox(langs: List<String>): JComponent =
        Box.createHorizontalBox().also { parent ->
            parent.add(JLabel("选择语言类型:").labelSize())
            parent.add(Box.createHorizontalGlue())
            val verticalBox = Box.createVerticalBox()
            parent.add(verticalBox)
            var lineView: Box? = null
            langsJCheckBoxList = langs.map { JCheckBox(it).apply { isSelected = true } }
            langsJCheckBoxList.forEachIndexed { index, jCheckBox ->
                if ((index) % 10 == 0) {
                    lineView = Box.createHorizontalBox()
                    verticalBox.add(lineView)
                }
                lineView?.add(jCheckBox)
            }
        }

    //所有模块
    private fun createModulesBox(modules: List<String>): JComponent =
        Box.createHorizontalBox().also { parent ->
            parent.add(JLabel("选择模块:").labelSize())
            parent.add(Box.createHorizontalGlue())
            val verticalBox = Box.createVerticalBox()
            parent.add(verticalBox)
            var lineView: Box? = null
            moduleJCheckBoxList = modules.map { JCheckBox(it).apply { isSelected = true } }
            moduleJCheckBoxList.forEachIndexed { index, jCheckBox ->
                if ((index) % 5 == 0) {
                    lineView = Box.createHorizontalBox()
                    verticalBox.add(lineView)
                }
                lineView?.add(jCheckBox)
            }
        }

    //选择文件
    private fun createFilePathBox(): JComponent =
        Box.createHorizontalBox().apply {
            add(JLabel("Excel文件路径:").labelSize())
            add(Box.createHorizontalGlue())
            add(Box.createHorizontalBox().apply {
                add(JTextField().apply { minimumSize = Dimension(300, 10) })
                add(Box.createHorizontalStrut(10))
                add(JButton("选择文件").apply {
                    addActionListener {
                        //选择文件
                    }
                })
            })
        }

    //导入模式
    private fun createModelTypeBox(): JComponent =
        Box.createHorizontalBox().also { box ->
            box.add(JLabel("选择导入模式:").labelSize())
            box.add(Box.createHorizontalGlue())
            ButtonGroup().also { group ->
                exportRB = JRadioButton("导出")
                exportRB?.isSelected = true
                group.add(exportRB)
                importRB = JRadioButton("导入")
                group.add(importRB)
            }
            box.add(exportRB)
            box.add(importRB)
        }

    override fun doValidate(): ValidationInfo? {
        val excel = excelPath?.text ?: ""
        if (!File(excel).exists()) {
            return ValidationInfo("Excel文件地址不存在！")
        }
        val moduleNames = moduleJCheckBoxList.filter { it.isSelected }.map { it.text }
        if (moduleNames.isEmpty()) {
            return ValidationInfo("请至少选择一个module！")
        }
        val langNames = langsJCheckBoxList.filter { it.isSelected }.map { it.text }
        if (langNames.isEmpty()) {
            return ValidationInfo("请至少选择一个语言类型！")
        }
        return null
    }

}