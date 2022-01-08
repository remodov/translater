package com.translater.translate.yandex.service

import com.translater.translate.abstractservice.TranslationClient
import com.translater.translate.yandex.model.TranslationYandexRequest
import com.translater.translate.yandex.model.TranslationYandexResponse
import com.translater.translate.yandex.properties.YandexPartConnectionProperties
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.lang.Thread.sleep

@Component
class TranslationYandexClientImpl(
    val partConnectionProperties: YandexPartConnectionProperties,
    val yandexRestTemplate: RestTemplate
) : TranslationClient {
    companion object {
        const val MAX_TRANSLATION_SIZE = 10000
    }

    override fun translate(textForTranslation: String, sourceLanguage: String, targetLanguage: String): String {
        sleep(1000)
        return if (textForTranslation.length > MAX_TRANSLATION_SIZE) {
            val sb = StringBuilder()
            textForTranslation.chunked(MAX_TRANSLATION_SIZE)
                .map { performRequest(it, sourceLanguage, targetLanguage) }
                .forEach { sb.append(it) }
            sb.toString()

        } else {
            performRequest(textForTranslation, sourceLanguage, targetLanguage)
        }
    }

    private fun createRequest(
        textForTranslation: String,
        sourceLanguage: String,
        targetLanguage: String
    ): TranslationYandexRequest {
        return TranslationYandexRequest().apply {
            folderId = partConnectionProperties.folderId
            sourceLanguageCode = sourceLanguage
            targetLanguageCode = targetLanguage
            texts.add(textForTranslation)
        }
    }

    private fun performRequest(textForTranslation: String, sourceLanguage: String, targetLanguage: String): String {
        val translationYandexRequest = createRequest(textForTranslation, sourceLanguage, targetLanguage)

        val translationYandexResponse: TranslationYandexResponse? = yandexRestTemplate.postForObject(
            partConnectionProperties.apiUrl!!, translationYandexRequest,
            TranslationYandexResponse::class.java
        )

        return translationYandexResponse?.translations
            ?.map { it.text }
            ?.joinToString(" ") ?: ""
    }
}