package com.translater.translate.yandex.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "translate.yandex")
data class YandexPartConnectionProperties(
    var apiKey: String? = null,
    var apiUrl: String? = null,
    var folderId: String? = null
)