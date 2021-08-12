package ru.translater.common.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonConfiguration {

    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

}
