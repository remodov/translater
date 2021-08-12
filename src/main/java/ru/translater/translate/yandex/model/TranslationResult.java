package ru.translater.translate.yandex.model;

import lombok.Data;

@Data
public class TranslationResult {
    private String text;
    private String detectedLanguageCode;
}
