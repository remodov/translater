package com.translater.format.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.translater.common.model.ExecutionTask
import com.translater.common.model.Meta
import com.translater.format.model.Page
import com.translater.format.model.PageAnalyzeRules
import com.translater.format.model.Rule
import com.translater.format.properties.FormatProperties
import com.translater.format.utils.ImageUtils
import mu.KLogging
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.springframework.stereotype.Component
import java.io.File
import java.io.IOException
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDate
import java.util.function.Consumer
import javax.imageio.ImageIO

@Component
class FormatTaskService(
    val formatProperties: FormatProperties,
    val objectMapper: ObjectMapper
) : ExecutionTask {

    companion object : KLogging()

    override fun start() {
        logger.info("Start format task.")

        logger.info("Load format task: {}", formatProperties.formatTemplatePath)
        val rules: List<Rule> = objectMapper.readValue(
            File(formatProperties.formatTemplatePath),
            PageAnalyzeRules::class.java
        ).rules
        logger.info("Loaded rules: {}", rules.size)

        val idLinks = File(formatProperties.analyzePath).list()
        logger.info { "Pages for analyze: ${idLinks.size}" }

        idLinks
            .map { path: String ->
                path.split("\\.".toRegex()).toTypedArray()[0]
            }.forEach { idLink ->
                logger.info { "Start analyze page: $idLink}" }
                try {
                    val htmlPageForAnalyze = java.lang.String.join(
                        "",
                        Files.readAllLines(
                            File(
                                formatProperties.analyzePath + File.separator + idLink + ".html"
                            ).toPath()
                        )
                    )
                    val documentForAnalyze = Jsoup.parse(htmlPageForAnalyze)
                    val payload = objectMapper.createObjectNode()
                    val page = Page().apply {
                        //TODO source url in collect like idlink   page.setSourceUrl("https://www.greatbritishchefs.com/recipes/" + idLink.replaceAll("recipes-", "").replaceAll("greatbritishchefs-com-",""));
                        this.payload = payload
                        generatedDate = LocalDate.now().toString()
                        uniqueId = idLink
                        pictureUrl = "$idLink.jpg"
                    }

                    //meta-tags
                    val metaTags = documentForAnalyze.getElementsByTag("meta")
                    metaTags.forEach(Consumer { metaTag: Element ->
                        Meta().apply {
                            content = metaTag.attr("content")
                            name = metaTag.attr("name")
                            property = metaTag.attr("itemprop")
                        }.let {
                            page.metas.add(it)
                        }
                    })
                    //payload
                    rules.forEach(Consumer { rule ->
                        var elements = Elements(documentForAnalyze)
                        val foundedElements = Elements()
                        rule.steps.forEach { step ->
                            elements.forEach(Consumer { element: Element ->
                                if (step.searchType == "id") {
                                    val elementById = element.getElementById(step.searchValue)
                                    if (elementById != null) {
                                        foundedElements.add(elementById)
                                    }
                                }
                                if (step.searchType == "class") {
                                    foundedElements.addAll(element.getElementsByClass(step.searchValue))
                                }
                                if (step.searchType == "element") {
                                    foundedElements.addAll(element.getElementsByTag(step.searchValue))
                                }
                            })
                            elements = Elements(foundedElements)
                            foundedElements.clear()
                        }

                        if (rule.type == "list") {
                            val arrayNode = objectMapper.createArrayNode()
                            elements.forEach(Consumer { element ->
                                arrayNode.add(
                                    element.text()
                                )
                            })
                            payload.set<ObjectNode>(rule.fieldName, arrayNode)
                        }
                        if (rule.type == "element" && elements.isNotEmpty()) {
                            payload.put(rule.fieldName, elements[0].text())
                        }
                        if (rule.type == "img" && elements.isNotEmpty()) {
                            payload.put(rule.fieldName, loadImageFromElement(elements[0], idLink))
                        }
                        if (rule.type == "img-list") {
                            val arrayNode = objectMapper.createArrayNode()
                            elements.forEachIndexed { index, element ->
                                val linkNumber = index + 1
                                arrayNode.add(loadImageFromElement(element, idLink + linkNumber))
                            }

                            payload.set<ObjectNode>(rule.fieldName, arrayNode)
                        }
                    })
                    objectMapper.writeValue(
                        File(
                            formatProperties.storePath + File.separator + idLink + ".json"
                        ), page
                    )
                } catch (e: Exception) {
                    logger.error { "Error analyze page: $idLink" }
                    logger.error(e.localizedMessage, e)
                }
                logger.info { "End analyze page: $idLink" }
            }

    }

    private fun loadImageFromElement(element: Element, idLink: String): String? {
        var imgSrc = element.attr("src")
        if (imgSrc.startsWith("//")) {
            imgSrc = "https://${imgSrc.substring(2)}"
        }
        return try {
            val read = ImageIO.read(URL(imgSrc))
            ImageIO.write(
                read,
                "jpg",
                Paths.get("${formatProperties.storePath}${File.separator}$idLink.jpg").toFile()
            )
            ImageUtils.main(
                Paths.get("${formatProperties.storePath}${File.separator}$idLink.jpg").toFile()
                    .toString()
            )
            "$idLink.jpg"
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

}
