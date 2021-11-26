package com.translater.collect.properties

import com.translater.common.properties.CommonProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "collect")
data class CollectProperties(
    override val storePath: String,
    val numberOfCrawlers: Int,
    val seedUrls: List<String>,
    val filterUrls: List<String>,
    val notFilterUrls: List<String>,
    val notAvailableExtensions: String
) : CommonProperties(storePath)
