package com.lucas.lang.utils

import com.google.gson.Gson
import com.lucas.lang.bean.OnlineTransBean
import com.lucas.lang.bean.TransResultBean
import com.lucas.lang.exception.ParserPluginException
import com.lucas.lang.ext.log
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import java.io.UnsupportedEncodingException
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

object OnlineTranslationHelper {
    private val client by lazy {
        OkHttpClient()
            .newBuilder()
            .addInterceptor(HttpLoggingInterceptor { netLogCallback?.invoke(it) })
            .build()
    }
    private val gson = Gson()

    //请求失败重试次数
    private var retryCount = 3

    var netLogCallback: ((msg: String) -> Unit)? = null

    //百度翻译配置
    private val baiduAppId = "20210924000954974"
    private val baiduKey = "4C8QDD77vcxKVjwTCJsn"

    //百度语言类型与Android的语言类型映射关系
    private val baiduLangTypeMap = mapOf(
        "zh" to "zh",
        "en" to "en",
        "ar" to "ara",
        "es" to "spa",
        "pt" to "pt",
    )

    //同步请求翻译
    fun transLang(text: String, fromLang: String, toLang: String): OnlineTransBean {
        if (!baiduLangTypeMap.containsKey(fromLang) || !baiduLangTypeMap.containsKey(toLang)) {
            throw ParserPluginException("baiduLangTypeMap未配置$fromLang or $toLang 映射关系！")
        }
        val realFromLang = baiduLangTypeMap[fromLang]!!
        val realToLang = baiduLangTypeMap[toLang]!!
        val response = client.newCall(buildGetBaiduUrl(text, realFromLang, realToLang)).execute()
        var errorMsg: String
        if (response.isSuccessful) {
            try {
                val message = response.body().string()
                log("[在线翻译]text:$text,$fromLang->$toLang,response:$message")
                val fromJson = gson.fromJson(message, OnlineTransBean::class.java)
                response.close()
                fromJson.apply {
                    isSuccess = error_code.isNullOrEmpty()
                    return retry(fromJson, text, fromLang, toLang)
                }
            } catch (e: Exception) {
                errorMsg = "数据解析错误：${e.message}"
                e.printStackTrace()
            }
        }
        response.close()
        errorMsg = "请求失败："
        val onlineTransBean =
            OnlineTransBean(false, realFromLang, realToLang, "1", errorMsg, arrayListOf(TransResultBean(text, "")))
        return retry(onlineTransBean, text, fromLang, toLang)
//        return onlineTransBean
    }

    private fun retry(
        fromJson: OnlineTransBean,
        text: String,
        fromLang: String,
        toLang: String
    ): OnlineTransBean {
        if (retryCount > 0 && !fromJson.isSuccess) {
            retryCount--
            return transLang(text, fromLang, toLang)
        } else {
            retryCount = 3
        }
        return fromJson
    }

    //创建一个get请求链接 -- get请求对特殊符号处理不太友好
    private fun buildGetBaiduUrl(text: String, fromLang: String, toLang: String): Request {
        val currentTimeMillis = System.currentTimeMillis()
        val url = "http://api.fanyi.baidu.com/api/trans/vip/translate"
            .plus("?q=$text")
            .plus("&from=$fromLang")
            .plus("&to=$toLang")
            .plus("&appid=$baiduAppId")
            .plus("&salt=$currentTimeMillis")
            .plus("&sign=${getMD5Str(baiduAppId.plus(text).plus(currentTimeMillis).plus(baiduKey))}")
        return Request.Builder().url(url).get().build()
    }

    //创建一个post请求链接
    private fun buildPostBaiduUrl(text: String, fromLang: String, toLang: String): Request {
        val currentTimeMillis = System.currentTimeMillis()
        val url = "http://api.fanyi.baidu.com/api/trans/vip/translate"
        val build = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("q", text)
            .addFormDataPart("from", fromLang)
            .addFormDataPart("to", toLang)
            .addFormDataPart("appid", baiduAppId)
            .addFormDataPart("salt", currentTimeMillis.toString())
            .addFormDataPart("sign", getMD5Str(baiduAppId.plus(text).plus(currentTimeMillis).plus(baiduKey)))
            .build()
        return Request.Builder().url(url).post(build).build()
    }

    private fun getMD5Str(str: String): String {
        var digest: ByteArray? = null
        try {
            val md5: MessageDigest = MessageDigest.getInstance("md5")
            digest = md5.digest(str.toByteArray(charset("utf-8")))
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        //16是表示转换为16进制数
        return BigInteger(1, digest).toString(16)
    }
}