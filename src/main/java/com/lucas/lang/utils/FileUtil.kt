package com.lucas.lang.utils

import java.io.File

object FileUtil {

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