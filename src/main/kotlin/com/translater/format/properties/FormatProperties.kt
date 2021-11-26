package com.translater.format.properties

import com.translater.common.properties.CommonProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "format")
class FormatProperties(
    override val storePath: String,
    val analyzePath: String,
    val formatTemplatePath: String
) : CommonProperties(storePath)