package ru.translater.translate.yandex;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;
import ru.translater.translate.TranslationClient;


public class TranslationClientYandexImplTest {
    private static YandexConnectionProperties connectionProperties;
    private static RestTemplate yandexRestTemplate;
    private static TranslationClient translationClient;

    @BeforeAll
    public static void init() {
        connectionProperties = new YandexConnectionProperties();
        connectionProperties.setApiKey("AQVN3VCmaRe8Tg11I4GgWO2j3AhzchypLHTBlRRW");
        connectionProperties.setApiUrl("https://translate.api.cloud.yandex.net/translate/v2/translate");
        connectionProperties.setFolderId("b1gusjhb3qsdmgm69fc8");

        yandexRestTemplate = new YandexTranslationConfiguration().yandexRestTemplate(connectionProperties);
        translationClient = new TranslationClientYandexImpl(connectionProperties, yandexRestTemplate);
    }

    @Test
    public void testSuccessTranslation() {
        String translatedText = translationClient.translate("Hello world!", "en", "ru");

        Assertions.assertEquals("Привет, мир!", translatedText);
    }
}