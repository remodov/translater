package com.translater.translate.abstractservice

interface TranslationClient {
    fun translate(textForTranslation: String, sourceLanguage: String, targetLanguage: String): String
}