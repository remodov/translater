package ru.translater.translation.yandex.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TranslationYandexRequest {
    private String sourceLanguageCode;
    private String targetLanguageCode;
    private String format;
    private String folderId;
    private List<String> texts = new ArrayList<>();
}
