package com.example.demo

enum class BookStatus(val code: Int) {
    UNPUBLISHED(0), // 未出版
    PUBLISHED(1);   // 出版済み

    companion object {
        fun fromCode(code: Int) = values().find { it.code == code }
            ?: throw IllegalArgumentException("不正なステータスコード: $code")
    }
}