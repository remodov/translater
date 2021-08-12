package ru.translater.translate.yandex;


import org.apache.http.HttpHeaders;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class YandexTranslationConfiguration {
    private final String AUTHORIZATION_API_KEY = "Api-Key";

    @Bean
    public RestTemplate yandexRestTemplate(YandexConnectionProperties connectionProperties) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setMessageConverters(getMessageConverters());
        restTemplate.getInterceptors().add(
                (request, body, execution) -> {
                    request.getHeaders().add(HttpHeaders.AUTHORIZATION, AUTHORIZATION_API_KEY + " " + connectionProperties.getApiKey());
                    return execution.execute(request, body);
                }
        );

        return restTemplate;
    }

    private List<HttpMessageConverter<?>> getMessageConverters() {
        List<HttpMessageConverter<?>> converters =
                new ArrayList<>();
        converters.add(new MappingJackson2HttpMessageConverter());
        return converters;
    }

}
