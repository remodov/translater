package com.translater.generate.properties

import com.translater.common.properties.CommonProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "generate")
data class GenerateProperties(
    override val storePath: String,
    val formatStorePath: String,
    val imagePath: String,
    val configTemplatePath: String
) : CommonProperties(storePath)