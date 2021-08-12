package ru.translater.translate;

public interface TranslationClient {
    String translate(String textForTranslation, String sourceLanguage, String targetLanguage);
}
