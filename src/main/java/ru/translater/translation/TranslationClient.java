package ru.translater.translation;

public interface TranslationClient {
    String translate(String textForTranslation, String sourceLanguage, String targetLanguage);
}
