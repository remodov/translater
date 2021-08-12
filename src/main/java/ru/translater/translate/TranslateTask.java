package ru.translater.translate;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.translater.common.ExecutionTask;
import ru.translater.format.model.Page;
import ru.translater.format.model.PageAnalyzeRules;
import ru.translater.format.model.Rule;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor
@Component
public class TranslateTask implements ExecutionTask {
    private final TranslateProperties translateProperties;
    private final TranslationClient translationClient;
    private final ObjectMapper objectMapper;

    @Override
    @SneakyThrows
    public void start() {
        log.info("Start TranslateTask task.");

        log.info("Load configurations for translate: {}", translateProperties.getConfigPath());
        List<Rule> rules = objectMapper.readValue(new File(translateProperties.getConfigPath()), PageAnalyzeRules.class).getRules();
        log.info("Loaded rules: {}", rules.size());

        Set<String> idLinks = Stream.of(Objects.requireNonNull(new File(translateProperties.getAnalyzePath()).list()))
                .filter(idLink -> idLink.contains(".json"))
                .collect(Collectors.toSet());

        Set<String> idLinksAlreadyAnalyzed = Stream.of(Objects.requireNonNull(new File(translateProperties.getStorePath()).list()))
                .filter(idLink -> idLink.contains(".json"))
                .collect(Collectors.toSet());

        idLinks.removeAll(idLinksAlreadyAnalyzed);

        log.info("Pages for analyze: {}", idLinks.size());

        idLinks.forEach(idLink -> {
            log.info("Start page translate: {}", idLink);

            try {
                Page page = objectMapper.readValue(new File(translateProperties.getAnalyzePath() + File.separator + idLink), Page.class);
                JsonNode payload = page.getPayload();
                ObjectNode translatedPayload = objectMapper.createObjectNode();

                rules.forEach(rule -> {
                    String type = rule.getType();
                    String fieldName = rule.getFieldName();
                    String isTranslated = rule.getIsTranslated();

                    if (Objects.equals(type, "element")) {
                        String fieldValue = payload.get(fieldName).textValue();
                        if (Objects.equals(isTranslated, "true")) {
                            fieldValue = translationClient.translate(fieldValue, "en", "ru");
                        }
                        translatedPayload.put(rule.getFieldName(), fieldValue);
                    }

                    if (Objects.equals(type, "list")) {
                        ArrayNode arrayNode = (ArrayNode) payload.get(fieldName);

                        if (Objects.equals(isTranslated, "true")) {
                            ArrayNode translatedArrayNode = objectMapper.createArrayNode();
                            arrayNode.forEach(arrayNodeValue -> {
                                translatedArrayNode.add(translationClient.translate(arrayNodeValue.textValue(), "en", "ru"));
                            });
                            arrayNode = translatedArrayNode;
                        }
                        translatedPayload.set(rule.getFieldName(), arrayNode);
                    }
                });

                page.setPayload(translatedPayload);
                objectMapper.writeValue(new File(translateProperties.getStorePath() + File.separator + idLink), page);

                log.info("End page translate: {}", idLink);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        log.info("End translation task.");
    }
}
