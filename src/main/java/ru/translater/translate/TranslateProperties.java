package ru.translater.translate;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import ru.translater.common.CommonProperties;
import ru.translater.translate.yandex.YandexConnectionProperties;

@Data
@Configuration
@ConfigurationProperties(prefix = "translate")
public class TranslateProperties extends CommonProperties {
    private String analyzePath;
    private YandexConnectionProperties yandexConnectionProperties;
    private String configPath;
}

