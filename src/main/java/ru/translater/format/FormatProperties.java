package ru.translater.format;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import ru.translater.common.CommonProperties;

@Data
@Configuration
@ConfigurationProperties(prefix = "format")
public class FormatProperties extends CommonProperties {
    private String analyzePath;
    private String formatTemplatePath;
}

