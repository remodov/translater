package com.translater.translate.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.translater.common.model.ExecutionTask
import com.translater.format.model.Page
import com.translater.format.model.PageAnalyzeRules
import com.translater.format.model.Rule
import com.translater.translate.LanguageProperties
import com.translater.translate.TranslateProperties
import com.translater.translate.abstractservice.TranslationClient
import mu.KLogging
import org.springframework.stereotype.Component
import java.io.File
import java.io.IOException
import java.util.function.Consumer

@Component
class TranslateTaskService(
    private val translateProperties: TranslateProperties,
    val translationClient: TranslationClient,
    val objectMapper: ObjectMapper,
    val languageProperties: LanguageProperties
) : ExecutionTask {
    companion object : KLogging() {
        const val LIST_ITEM_DELIMITER: String = "##"
    }

    override fun start() {
        languageProperties.languages.values
            .forEach { lang ->
                checkLangDirectory(lang)
                translateAndSave(translateProperties.sourceLanguage, lang)
            }
    }

    private fun translateAndSave(sourceLanguage: String, targetLanguage: String) {
        logger.info { "Start TranslateTask task for language: $targetLanguage" }

        logger.info { "Load configurations for translate:  ${translateProperties.configPath}" }
        val rules: List<Rule> = objectMapper.readValue(
            File(translateProperties.configPath),
            PageAnalyzeRules::class.java
        ).rules
        logger.info { "Loaded rules: ${rules.size}" }


        val idLinks = File(translateProperties.analyzePath).list()!!
            .filter { it.contains(".json") }
            .toMutableSet()

        val idLinksAlreadyAnalyzed = File(translateProperties.storePath).list()!!
            .filter { it.contains(".json") }
            .toMutableSet()

        idLinks.removeAll(idLinksAlreadyAnalyzed)

        logger.info { "Pages for analyze: ${idLinks.size}" }

        idLinks.forEach(Consumer { idLink: String ->
            logger.info { "Start page translate: $idLink" }
            try {
                val page: Page = objectMapper.readValue(
                    File("${translateProperties.analyzePath}${File.separator}$idLink"),
                    Page::class.java
                )
                val payload: JsonNode = page.payload!!
                val translatedPayload = objectMapper.createObjectNode()
                rules.forEach(Consumer pointer@{ rule: Rule ->
                    val type: String = rule.type
                    val fieldName: String = rule.fieldName
                    val isTranslated: String = rule.isTranslated
                    if (type == "element" && !payload.isEmpty) {
                        if (payload[fieldName] == null) return@pointer

                        var fieldValue = payload[fieldName].textValue()
                        if (isTranslated == "false") {
                            fieldValue = translationClient.translate(fieldValue!!, sourceLanguage, targetLanguage)
                        }
                        translatedPayload.put(rule.fieldName, fieldValue)
                    }
                    if (type == "list" && !payload.isEmpty) {
                        if (payload[fieldName] == null) return@pointer
                        var arrayNode = payload[fieldName] as ArrayNode
                        if (isTranslated == "false") {
                            val listItems = arrayNode.joinToString(LIST_ITEM_DELIMITER) { it.textValue() }
                            if (listItems.isEmpty()) return@pointer
                            val translatedArrayNode = objectMapper.createArrayNode()
                            val translatedListItems =
                                translationClient.translate(listItems, sourceLanguage, targetLanguage)
                            translatedListItems.split(LIST_ITEM_DELIMITER).forEach {
                                translatedArrayNode.add(it)
                            }
                            arrayNode = translatedArrayNode
                        }
                        translatedPayload.set<ObjectNode>(rule.fieldName, arrayNode)
                    }
                })
                page.payload = translatedPayload
                objectMapper.writeValue(
                    File(
                        "${translateProperties.storePath}${File.separator}" +
                                "$targetLanguage${File.separator}$idLink"
                    ), page
                )
                logger.info { "End page translate: $idLink" }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        })
    }

    private fun checkLangDirectory(targetLanguage: String) {
        val tempDirectory = File("${translateProperties.storePath}${File.separator}$targetLanguage")
        if (tempDirectory.exists()) {
            return
        }
        tempDirectory.mkdir()
    }
}