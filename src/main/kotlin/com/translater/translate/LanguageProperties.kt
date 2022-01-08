package com.translater.translate

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "language")
class LanguageProperties(
    private val options: List<String>
) {
    val languages: Map<String, String> by lazy {
       return@lazy options.associateWith { it }
    }
}