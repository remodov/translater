package com.translater.collect.service

import com.goikosoft.crawler4j.crawler.Page
import com.goikosoft.crawler4j.crawler.WebCrawler
import com.goikosoft.crawler4j.parser.HtmlParseData
import com.goikosoft.crawler4j.url.WebURL
import com.translater.collect.properties.CollectProperties
import mu.KLogging
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import java.util.regex.Pattern

class Crawler(
    private val collectProperties: CollectProperties
) : WebCrawler() {

    companion object : KLogging()

    private val notAvailableExtensionsFilter: Pattern by lazy {
        Pattern.compile(collectProperties.notAvailableExtensions)
    }


    override fun shouldVisit(referringPage: Page?, url: WebURL): Boolean {
        val href = url.url.lowercase(Locale.getDefault())
        return !notAvailableExtensionsFilter.matcher(href).matches()
                && isContainsAllFilters(href)
                && isContainsNoAllFilters(href)
    }

    private fun isContainsNoAllFilters(href: String): Boolean {
        collectProperties.notFilterUrls.forEach {
            if (it in href) return false
        }
        return true
    }


    private fun isContainsAllFilters(href: String): Boolean {
        collectProperties.filterUrls.forEach {
            if (it !in href) return false
        }
        return true
    }

    override fun visit(page: Page) {
        val url = page.webURL.url
        logger.debug { "URL: $url" }
        if (!isContainsAllFilters(url)) {
            logger.debug { "URL does not contains all filters: $url" }
            return
        }
        if (page.parseData is HtmlParseData) {
            val htmlParseData = page.parseData as HtmlParseData
            val crawlStorageFolder = myController.config.crawlStorageFolder
            try {
                val path = Paths.get(
                    crawlStorageFolder,
                    page.webURL.url
                        .replace("https://www.".toRegex(), "")
                        .replace("https://".toRegex(), "")
                        .replace("\\.".toRegex(), "-")
                        .replace("/".toRegex(), "-")
                )
                Files.write(Paths.get("$path.html"), htmlParseData.html.toByteArray(StandardCharsets.UTF_8))
            } catch (e: IOException) {
                logger.error { "Error when parsing page: $url error message: ${e.localizedMessage}  $e" }
            }
        }
    }
}