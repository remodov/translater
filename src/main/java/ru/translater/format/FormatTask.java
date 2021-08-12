package ru.translater.format;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import ru.translater.common.ExecutionTask;
import ru.translater.common.model.Meta;
import ru.translater.format.model.Page;
import ru.translater.format.model.PageAnalyzeRules;
import ru.translater.format.model.Rule;
import ru.translater.format.model.Step;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Slf4j
@Component
public class FormatTask implements ExecutionTask {
    private final FormatProperties formatProperties;
    private final ObjectMapper objectMapper;

    @Override
    @SneakyThrows
    public void start() {
        log.info("Start format task.");

        log.info("Load format task: {}", formatProperties.getFormatTemplatePath());
        List<Rule> rules = objectMapper.readValue(new File(formatProperties.getFormatTemplatePath()), PageAnalyzeRules.class).getRules();
        log.info("Loaded rules: {}", rules.size());

        Set<String> idLinks = Stream.of(Objects.requireNonNull(new File(formatProperties.getAnalyzePath()).list()))
                .map(path -> path.split("\\.")[0])
                .collect(Collectors.toSet());

        log.info("Pages for analyze: {}", idLinks.size());

        idLinks.forEach(idLink -> {
            log.info("Start analyze page: {}", idLink);
            try {
                String htmlPageForAnalyze =
                        String.join("", Files.readAllLines(new File(formatProperties.getAnalyzePath() + File.separator + idLink + ".html").toPath()));

                Document documentForAnalyze = Jsoup.parse(htmlPageForAnalyze);
                ObjectNode payload = objectMapper.createObjectNode();
                Page page = new Page();
                page.setGeneratedDate(LocalDateTime.now().toString());
                page.setUniqueId(idLink);
//TODO source url in collect like idlink   page.setSourceUrl("https://www.greatbritishchefs.com/recipes/" + idLink.replaceAll("recipes-", "").replaceAll("greatbritishchefs-com-",""));
                page.setPayload(payload);
                page.setPictureUrl(idLink + ".jpg");

                //meta-tags

                Elements metaTags = documentForAnalyze.getElementsByTag("meta");
                metaTags.forEach(metaTag -> {
                    Meta meta = new Meta();
                    meta.setContent(metaTag.attr("content"));
                    meta.setName(metaTag.attr("name"));
                    meta.setProperty(metaTag.attr("itemprop"));
                    page.getMetas().add(meta);
                });
                //payload
                rules.forEach(rule -> {
                    Elements elements = new Elements(documentForAnalyze);
                    Elements foundedElements = new Elements();

                    for (Step step : rule.getSteps()) {
                        elements.forEach(element -> {
                            if (Objects.equals(step.getSearchType(), "id")) {
                                Element elementById = element.getElementById(step.getSearchValue());
                                if (elementById != null) {
                                    foundedElements.add(elementById);
                                }
                            }

                            if (Objects.equals(step.getSearchType(), "class")) {
                                foundedElements.addAll(element.getElementsByClass(step.getSearchValue()));
                            }

                            if (Objects.equals(step.getSearchType(), "element")) {
                                foundedElements.addAll(element.getElementsByTag(step.getSearchValue()));
                            }

                        });

                        elements = new Elements(foundedElements);
                        foundedElements.clear();
                    }

                    if (Objects.equals(rule.getType(), "list")) {
                        ArrayNode arrayNode = objectMapper.createArrayNode();
                        elements.forEach(element -> {
                            arrayNode.add(element.text());
                        });
                        payload.set(rule.getFieldName(), arrayNode);
                    }

                    if (Objects.equals(rule.getType(), "element")) {
                        payload.put(rule.getFieldName(), elements.get(0).text());
                    }

                    if (Objects.equals(rule.getType(), "img")) {
                        payload.put(rule.getFieldName(), loadImageFromElement(elements.get(0), idLink));
                    }
                    if (Objects.equals(rule.getType(), "img-list")) {
                        ArrayNode arrayNode = objectMapper.createArrayNode();
                        int i = 1;
                        for (Element element : elements) {
                            arrayNode.add(loadImageFromElement(element, idLink + i++));
                        }
                        payload.set(rule.getFieldName(), arrayNode);
                    }
                });

                objectMapper.writeValue(new File(formatProperties.getStorePath() + File.separator + idLink + ".json"), page);

            } catch (Exception e) {
                log.error("Error analyze page: {}", idLink);
                log.error(e.getLocalizedMessage(), e);
            }
            log.info("End analyze page: {}", idLink);
        });

        log.info("End format task.");
    }

    private String loadImageFromElement(Element element, String idLink) {
        String imgSrc = element.attr("src");
        if (imgSrc.startsWith("//")) {
            imgSrc = "https://" + imgSrc.substring(2);
        }

        try {
            BufferedImage read = ImageIO.read(new URL(imgSrc));

            ImageIO.write(read, "jpg", Paths.get(formatProperties.getStorePath() + File.separator +idLink + ".jpg").toFile());

            ImageUtils.main(Paths.get(formatProperties.getStorePath() + File.separator + idLink + ".jpg").toFile().toString());

            return idLink + ".jpg";
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
