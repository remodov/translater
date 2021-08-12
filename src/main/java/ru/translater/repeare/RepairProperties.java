package ru.translater.repeare;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import ru.translater.common.CommonProperties;
import ru.translater.translate.yandex.YandexConnectionProperties;

@Data
@Configuration
@ConfigurationProperties(prefix = "repair")
public class RepairProperties extends CommonProperties {
    private String analyzePath;
    private String configPath;
}

