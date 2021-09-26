package com.lucas.lang.bean


data class OnlineTransBean(
    var isSuccess: Boolean,
    val from: String,
    val to: String,
    val error_code: String,
    val error_msg: String,
    val trans_result: List<TransResultBean>,
){
    override fun toString(): String {
        return "OnlineTransBean(isSuccess=$isSuccess, from='$from', to='$to', trans_result=$trans_result)"
    }
}

data class TransResultBean(
    val src: String,
    val dst: String,
){
    override fun toString(): String {
        return "TransResultBean(src='$src', dst='$dst')"
    }
}
