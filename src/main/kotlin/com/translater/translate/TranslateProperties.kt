package com.translater.translate

import com.translater.translate.yandex.properties.YandexPartConnectionProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "translate")
class TranslateProperties(
    val yandex: YandexPartConnectionProperties,
    val storePath: String,
    val analyzePath: String,
    val configPath: String,
    val sourceLanguage: String
)