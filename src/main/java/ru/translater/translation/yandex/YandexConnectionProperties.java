package ru.translater.translation.yandex;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "yandex")
public class YandexConnectionProperties {
    private String apiKey;
    private String apiUrl;
    private String folderId;
}
