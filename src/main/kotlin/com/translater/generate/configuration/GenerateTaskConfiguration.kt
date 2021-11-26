package com.translater.generate.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.thymeleaf.TemplateEngine
import org.thymeleaf.templatemode.TemplateMode
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver
import org.thymeleaf.templateresolver.ITemplateResolver

@Configuration
class GenerateTaskConfiguration {

    @Bean
    fun fileTemplateEngine(): TemplateEngine {
        return TemplateEngine().apply {
            setTemplateResolver(getTemplateResolver())
        }
    }

    @Bean
    fun secondaryTemplateResolver(): ClassLoaderTemplateResolver? {
        val secondaryTemplateResolver = ClassLoaderTemplateResolver()
        secondaryTemplateResolver.prefix = "template/"
        secondaryTemplateResolver.suffix = ".html"
        secondaryTemplateResolver.templateMode = TemplateMode.HTML
        secondaryTemplateResolver.characterEncoding = "UTF-8"
        secondaryTemplateResolver.order = 1
        secondaryTemplateResolver.checkExistence = true
        return secondaryTemplateResolver
    }

    private fun getTemplateResolver(): ITemplateResolver {
        return ClassLoaderTemplateResolver().apply {
            prefix = "/template/"
            suffix = ".html"
            characterEncoding = "UTF-8"
            templateMode = TemplateMode.HTML
        }
    }
}