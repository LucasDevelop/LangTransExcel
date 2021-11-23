package com.lucas.lang.utils

import com.google.gson.Gson
import java.io.File

object CacheUtil {
    const val cacheFileName = "langConfig.json"
    val gson = Gson()

    fun saveConfig(parserConfig: ParserConfig) {
        val file = File(parserConfig.projectPath, cacheFileName)
        val json = gson.toJson(parserConfig)
        file.writeText(json)
    }

    fun readConfig(projectPath:String): ParserConfig? {
        val file = File(projectPath, cacheFileName)
        if (!file.exists()) return null
        val json = file.readText()
        return gson.fromJson(json, ParserConfig::class.java)
    }
}