package ru.translater.translation;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class TranslationService {

    private static final String IAM_TOKEN = "AQVN3VCmaRe8Tg11I4GgWO2j3AhzchypLHTBlRRW";
    private static final String AUTH_HEADER = "Api-Key " + IAM_TOKEN;

    public String translate(String textForTranslate) throws IOException {
        String json = "{" +
                "  \"sourceLanguageCode\": \"ru\"," +
                "  \"targetLanguageCode\": \"en\"," +
                "  \"format\": \"HTML\"," +
                "  \"folderId\": \"b1gusjhb3qsdmgm69fc8\"," +
                "  \"texts\": [" +
                "    \"" + textForTranslate + "\"" +
                "  ]" +
                "}";

        try(CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost("https://translate.api.cloud.yandex.net/translate/v2/translate");
            httpPost.addHeader("Authorization", AUTH_HEADER);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            httpPost.setHeader("Accept-Encoding", "UTF-8");
            httpPost.setEntity(new StringEntity(json, StandardCharsets.UTF_8));

            CloseableHttpResponse response = httpClient.execute(httpPost);

            return IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
        }
    }
}
