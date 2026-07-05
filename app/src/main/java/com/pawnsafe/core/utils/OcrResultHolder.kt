package com.pawnsafe.core.utils

object OcrResultHolder {
    private var result: OcrResult = OcrResult(emptyMap(), emptySet())

    fun set(r: OcrResult) { result = r }

    fun consume(): OcrResult {
        val r = result
        result = OcrResult(emptyMap(), emptySet())
        return r
    }
}