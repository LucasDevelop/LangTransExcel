package com.lucas.lang.utils

import com.lucas.lang.ext.isModuleDir
import java.io.File

object FileUtil {

    /**
     * 从项目中查找出所有的module文件夹
     */
    fun findAllModuleDir(projectFile: File,block:(File)->Unit): MutableList<File> {
        val list = mutableListOf<File>()
        findAllFileOrDir(projectFile, list) {
            block(it)
            it.isModuleDir()
        }
        return list
    }

    fun findAllFileOrDir(rootDir: File, list: MutableList<File>, trans: (File) -> Boolean) {
       if (rootDir.isDirectory&&rootDir.listFiles().isEmpty()){
           return
       }else{
           if (rootDir.isDirectory){
               rootDir.listFiles().forEach {
                   if (trans(it)) list.add(it)
                   if (it.isDirectory)
                       findAllFileOrDir(it,list,trans)
               }
           }else{
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