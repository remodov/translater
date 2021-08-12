package ru.translater.generate;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import ru.translater.common.CommonProperties;

@Data
@Configuration
@ConfigurationProperties(prefix = "generate")
public class GenerateProperties extends CommonProperties {
    private String formatStorePath;
    private String htmlTemplate;
    private String imagePath;
    private String configTemplatePath;
}
