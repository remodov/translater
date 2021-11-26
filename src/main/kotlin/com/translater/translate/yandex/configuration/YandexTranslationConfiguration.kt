package com.translater.translate.yandex.configuration

import com.translater.translate.yandex.properties.YandexPartConnectionProperties
import org.apache.http.HttpHeaders
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.client.RestTemplate

@Configuration
class YandexTranslationConfiguration {
    companion object {
        private const val AUTHORIZATION_API_KEY = "Api-Key"
    }

    @Bean
    fun yandexRestTemplate(partConnectionProperties: YandexPartConnectionProperties): RestTemplate {
        return RestTemplate().apply {
            messageConverters = getCustomMessageConverters()
            interceptors.add(
                ClientHttpRequestInterceptor { request: HttpRequest, body: ByteArray, execution: ClientHttpRequestExecution ->
                    request.headers.add(
                        HttpHeaders.AUTHORIZATION,
                        "$AUTHORIZATION_API_KEY ${partConnectionProperties.apiKey}"
                    )
                    execution.execute(request, body)
                }
            )
        }
    }

    private fun getCustomMessageConverters(): List<HttpMessageConverter<*>> {
        val converters: MutableList<HttpMessageConverter<*>> = mutableListOf()
        converters.add(MappingJackson2HttpMessageConverter())
        return converters
    }
}