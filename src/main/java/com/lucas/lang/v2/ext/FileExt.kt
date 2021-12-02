package com.lucas.lang.v2.ext

import java.io.File

//如果文件夹不存在则创建
fun File.ifExistsMakes(): File {
    if (name.contains(".")){
        if (!parentFile.exists())
        parentFile.mkdirs()
    }else{
        if (!exists())
        mkdirs()
    }
    return this
}
