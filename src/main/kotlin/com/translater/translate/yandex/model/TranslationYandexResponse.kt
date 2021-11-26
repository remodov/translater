package com.translater.translate.yandex.model

data class TranslationYandexResponse(
    var translations: List<TranslationResult> = ArrayList<TranslationResult>()
)