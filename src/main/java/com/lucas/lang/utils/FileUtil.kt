package com.lucas.lang.utils

import android.text.TextUtils
import com.lucas.lang.ext.isModuleDir
import com.lucas.lang.v2.bean.ModuleBean
import com.lucas.lang.v2.bean.XmlFileBean
import java.io.File
import java.util.regex.Pattern

object FileUtil {

    /**
     * 从项目中查找出所有的module文件夹
     */
    fun findAllModuleDir(projectFile: File, block: (File) -> Unit): MutableList<File> {
        val list = mutableListOf<File>()
        findAllFileOrDir(projectFile, list) {
            block(it)
            it.isModuleDir()
        }
        return list
    }

    fun findAllModule(projectFile: File, block: (File) -> Unit): MutableList<ModuleBean> {
        val list = mutableListOf<File>()
        findAllFileOrDir(projectFile, list) {
            block(it)
            it.isModuleDir()
        }
        return list.map { file ->
            ModuleBean().also { bean ->
                bean.moduleName = file.name
                bean.moduleFilePath = file.absolutePath
            }
        }.toMutableList()
    }

    //扫描module中的xml
    fun findLangByModule(allModuleDir: MutableList<ModuleBean>, block: (File) -> Unit) {
        for (i in allModuleDir.indices) {
            val langs = ArrayList<XmlFileBean>()
            val moduleBean = allModuleDir[i]
            val valuesDir = File(moduleBean.moduleFilePath, "src/main/res")
            if (valuesDir.exists() && valuesDir.isDirectory) { //查找语言类型
                for (file in valuesDir.listFiles()) {
                    val fileName = file.name
                    if (file.isDirectory && fileName.contains("-")) {
                        val langName = fileName.substring(fileName.indexOf("-") + 1)
                        if (!TextUtils.isEmpty(langName) &&
                            !langName.contains("dpi") &&
                            Pattern.compile("[a-z]{0,4}").matcher(langName).matches()
                        ) {
                            langs.add(XmlFileBean().also {
                                it.langName = langName
                                it.filePath = File(file.absolutePath, "strings.xml").absolutePath
                            })
                            moduleBean.xmlFiles = langs
                            block(file)
                        }
                    }
                }
            }
        }
    }

    //根据文件获取语言类型,可以是values-zh...或者strings.xml...
    fun getLangNameByFile(file: File): String? {
        if (!file.exists()) return null
        val dirName: String = if (file.isFile) {
            file.parentFile.name
        } else {
            file.name
        }
        val langName = dirName.substring(dirName.indexOf("-") + 1)
        return if (!TextUtils.isEmpty(langName) &&
            !langName.contains("dpi") &&
            Pattern.compile("[a-z]{0,4}").matcher(langName).matches()
        ){
            langName
        }else{
            null
        }
    }

    fun findAllFileOrDir(rootDir: File, list: MutableList<File>, trans: (File) -> Boolean) {
        if (rootDir.isDirectory && rootDir.listFiles().isEmpty()) {
            return
        } else {
            if (rootDir.isDirectory) {
                rootDir.listFiles().forEach {
                    if (trans(it)) list.add(it)
                    if (it.isDirectory)
                        findAllFileOrDir(it, list, trans)
                }
            } else {
                if (trans(rootDir)) list.add(rootDir)
            }
        }
    }

    /**
     * 更具条件查找出指定路径所有文件，包括子目录
     *
     * @param rootDir
     * @param list
     * @param trans
     */
    fun findAllFile(rootDir: File, list: MutableList<File>, trans: (File) -> Boolean) {
        if (rootDir.isFile) {
            if (trans(rootDir)) list.add(rootDir)
            return
        } else if (rootDir.isDirectory) {
            rootDir.listFiles()?.forEach {
                if (it.isFile) {
                    if (trans(it)) list.add(it)
                } else if (it.isDirectory) {
                    findAllFile(it, list, trans)
                }
            }
        }
    }


}