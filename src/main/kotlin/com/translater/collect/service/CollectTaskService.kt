package com.translater.collect.service

import com.goikosoft.crawler4j.crawler.CrawlConfig
import com.goikosoft.crawler4j.crawler.CrawlController
import com.goikosoft.crawler4j.fetcher.PageFetcher
import com.goikosoft.crawler4j.robotstxt.RobotstxtConfig
import com.goikosoft.crawler4j.robotstxt.RobotstxtServer
import com.translater.collect.properties.CollectProperties
import com.translater.common.model.ExecutionTask
import mu.KLogging
import org.springframework.stereotype.Component

@Component
class CollectTaskService(
    val collectProperties: CollectProperties
) : ExecutionTask {
    companion object : KLogging()

    override fun start() {
        logger.info("Start collection task.")

        CrawlConfig().let { config ->
            config.crawlStorageFolder = collectProperties.storePath
            PageFetcher(config).let { pageFetcher ->
                RobotstxtServer(RobotstxtConfig(), pageFetcher).let { robotsTxtServer ->
                    CrawlController(config, pageFetcher, robotsTxtServer).let { controller ->
                        collectProperties.seedUrls.forEach { seedUrl ->
                            controller.addSeed(seedUrl)
                        }
                        controller.start(
                            { Crawler(collectProperties) },
                            collectProperties.numberOfCrawlers
                        )
                    }
                }
            }
        }
    }
}