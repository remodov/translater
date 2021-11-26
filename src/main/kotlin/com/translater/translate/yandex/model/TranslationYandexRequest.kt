package com.translater.translate.yandex.model

data class TranslationYandexRequest(
    var sourceLanguageCode: String? = null,
    var targetLanguageCode: String? = null,
    var format: String? = null,
    var folderId: String? = null,
    var texts: MutableList<String> = mutableListOf()
)