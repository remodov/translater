package ru.translater;


import ru.translater.translation.TranslationService;

public class TranslateApplication {
    public static void main(String[] args) throws Exception {
        TranslationService translationService = new TranslationService();

        String result = translationService.translate("Я хочу перевести ");

        System.out.println(result);
    }
}
