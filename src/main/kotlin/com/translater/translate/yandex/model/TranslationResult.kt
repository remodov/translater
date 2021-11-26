package com.translater.translate.yandex.model

data class TranslationResult(
    var text: String? = null,
    var detectedLanguageCode: String? = null
)