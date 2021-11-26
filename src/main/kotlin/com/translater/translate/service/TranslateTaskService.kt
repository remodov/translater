package com.translater.translate.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.translater.common.model.ExecutionTask
import com.translater.format.model.Page
import com.translater.format.model.PageAnalyzeRules
import com.translater.format.model.Rule
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
) : ExecutionTask {
    companion object : KLogging()

    override fun start() {
        logger.info { "Start TranslateTask task." }

        logger.info { "Load configurations for translate:  ${translateProperties.configPath}" }
        val rules: List<Rule> = objectMapper.readValue(
            File(translateProperties.configPath!!),
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
                rules.forEach(Consumer { rule: Rule ->
                    val type: String = rule.type
                    val fieldName: String = rule.fieldName
                    val isTranslated: String = rule.isTranslated
                    if (type == "element") {
                        var fieldValue = payload[fieldName].textValue()
                        if (isTranslated == "true") {
                            fieldValue = translationClient.translate(fieldValue!!, "en", "ru")
                        }
                        translatedPayload.put(rule.fieldName, fieldValue)
                    }
                    if (type == "list") {
                        var arrayNode =
                            payload[fieldName] as ArrayNode
                        if (isTranslated == "true") {
                            val translatedArrayNode =
                                objectMapper.createArrayNode()
                            arrayNode.forEach(Consumer { arrayNodeValue: JsonNode ->
                                translatedArrayNode.add(
                                    translationClient.translate(arrayNodeValue.textValue(), "en", "ru")
                                )
                            })
                            arrayNode = translatedArrayNode
                        }
                        translatedPayload.set<ObjectNode>(rule.fieldName, arrayNode)
                    }
                })
                page.payload = translatedPayload
                objectMapper.writeValue(File("${translateProperties.storePath}${File.separator}$idLink"), page)
                logger.info{"End page translate: $idLink"}
            } catch (e: IOException) {
                e.printStackTrace()
            }
        })
    }
}