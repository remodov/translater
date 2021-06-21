package ru.translater.translation.yandex.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TranslationYandexResponse {
    private List<TranslationResult> translations = new ArrayList<>();
}
