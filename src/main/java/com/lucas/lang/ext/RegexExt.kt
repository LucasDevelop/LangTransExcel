package com.lucas.lang.ext

import java.io.File

//匹配模块规则--判断当前文件夹是否是模块的根目录
fun File.isModuleDir(): Boolean {
    if (!isDirectory) return false
    //包含至少一个.gradle文件
    val gradleFiles = listFiles().find { it.isFile && it.name.matches(Regex("^[a-zA-Z0-9-_]+.gradle$")) }
    //必须有src\main\java 或者 src\main\kotlin 路径
    val isContainResource = File(this, "src/main/java").exists() || File(this, "src/main/kotlin").exists()
    return isContainResource && gradleFiles != null
}