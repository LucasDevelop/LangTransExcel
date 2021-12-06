package com.lucas.lang.v2.ui

import com.android.tools.adtui.validation.ErrorDetailDialog
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.DialogWrapper
import com.lucas.lang.ext.labelSize
import com.lucas.lang.ui.ProgressLogDialog
import com.lucas.lang.utils.FileUtil
import com.lucas.lang.v2.confoig.ExportConfig
import com.lucas.lang.v2.confoig.IConfig
import com.lucas.lang.v2.confoig.InputConfig
import java.awt.Dimension
import java.io.File
import javax.swing.*

class SelectModelDialog(val anActionEvent: AnActionEvent) : DialogWrapper(true) {
    companion object {
        fun show(anActionEvent: AnActionEvent) {
            SelectModelDialog(anActionEvent).apply {
                pack()
                showAndGet()
            }
        }
    }

    init {
        init()
        title = "选择模式"
        setSize(300, 300)
    }

    private var exportRB: JRadioButton? = null
    private var importRB: JRadioButton? = null

    override fun createCenterPanel(): JComponent = Box.createVerticalBox().apply {
        add(createModelTypeBox())
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
            box.add(Box.createVerticalStrut(20))
            box.add(importRB)
        }

    override fun doOKAction() {
        super.doOKAction()
        val progressLogDialog = ProgressLogDialog.getInstance("初始化扫描")
        if (exportRB?.isSelected == true) {//导出
            val exportConfig = ExportConfig()
            scanProject(exportConfig, progressLogDialog) {
                ExportConfigDialog.show(exportConfig, anActionEvent)
            }
        } else {//导入
            val inputConfig = InputConfig()
            scanProject(inputConfig, progressLogDialog) {
                InputConfigDialog.show(anActionEvent, inputConfig)
            }
        }
    }

    private fun scanProject(
        config: IConfig,
        progressLogDialog: ProgressLogDialog,
        complete: () -> Unit
    ) {
        config.projectName = anActionEvent.project?.name ?: ""
        config.projectPath = anActionEvent.project?.basePath ?: ""
//        config.projectPath = "/Users/lucas/Documents/developer/android/EgyptOutfield/egypt-outfield-android"
        progressLogDialog.addLog("开始扫描项目文件")
        scanProjectFile(progressLogDialog, config) {
            progressLogDialog.dispose()
            complete()
        }
    }

    //扫描项目中资源
    private fun scanProjectFile(
        progressLogDialog: ProgressLogDialog,
        config: IConfig,
        onComplete: () -> Unit
    ) {
        Thread {
            FileUtil.findAllModule(File(config.projectPath)) { file ->
                progressLogDialog.addLog("File:${file.name},path:${file.absolutePath}")
            }.also {
                progressLogDialog.addLog("开始扫描语言资源")
                FileUtil.findLangByModule(it) { file ->
                    progressLogDialog.addLog("File:${file.name},path:${file.absolutePath}")
                }
                config.allModuleBeans = it
                progressLogDialog.addLog("扫描完成")
                SwingUtilities.invokeLater { onComplete() }
            }
        }.start()
        progressLogDialog.isVisible = true
    }

}