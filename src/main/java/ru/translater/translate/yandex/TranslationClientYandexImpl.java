package ru.translater.translate.yandex;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.translater.translate.TranslationClient;
import ru.translater.translate.yandex.model.TranslationResult;
import ru.translater.translate.yandex.model.TranslationYandexRequest;
import ru.translater.translate.yandex.model.TranslationYandexResponse;

import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class TranslationClientYandexImpl implements TranslationClient {
    private final YandexConnectionProperties connectionProperties;
    private final RestTemplate yandexRestTemplate;

    @Override
    public String translate(String textForTranslation, String sourceLanguage, String targetLanguage) {
        TranslationYandexRequest translationYandexRequest = createRequest(textForTranslation, sourceLanguage, targetLanguage);

        TranslationYandexResponse translationYandexResponse = yandexRestTemplate.postForObject(connectionProperties.getApiUrl(), translationYandexRequest, TranslationYandexResponse.class);

        return translationYandexResponse.getTranslations()
                .stream()
                .map(TranslationResult::getText)
                .collect(Collectors.joining(" "));
    }

    private TranslationYandexRequest createRequest(String textForTranslation, String sourceLanguage, String targetLanguage) {
        TranslationYandexRequest translationYandexRequest = new TranslationYandexRequest();
        translationYandexRequest.setFolderId(connectionProperties.getFolderId());
        translationYandexRequest.setSourceLanguageCode(sourceLanguage);
        translationYandexRequest.setTargetLanguageCode(targetLanguage);
        translationYandexRequest.getTexts().add(textForTranslation);
        return translationYandexRequest;
    }
}