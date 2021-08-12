package ru.translater.collect;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import ru.translater.common.CommonProperties;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "collect")
public class CollectProperties extends CommonProperties {
    private Integer numberOfCrawlers;
    private List<String> seedUrls;
    private List<String> filterUrls;
    private List<String> notFilterUrls;
}