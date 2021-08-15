package ru.translater.generate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redfin.sitemapgenerator.WebSitemapGenerator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import ru.translater.common.ExecutionTask;
import ru.translater.format.model.Page;

import javax.annotation.PostConstruct;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.file.Files.*;

@RequiredArgsConstructor
@Slf4j
@Component
public class GenerateTask implements ExecutionTask {
    private final GenerateProperties generateProperties;
    private final TemplateEngine templateEngine;
    private final ObjectMapper objectMapper;
    private Path pathForStore;
    private Path formatStorePath;
    private Path imagePath;

    @PostConstruct
    public void init() {
        pathForStore = Paths.get(generateProperties.getStorePath());
        formatStorePath = Paths.get(generateProperties.getFormatStorePath());
        imagePath = Paths.get(generateProperties.getImagePath());
    }

    @Override
    @SneakyThrows
    public void start() {
        log.info("Start generate task.");

        List<Page> commonPageModels = loadPagesSnapshotForGenerate();

        if (!exists(pathForStore)) {
            createDirectories(pathForStore);
        }

        if (!exists(formatStorePath)) {
            createDirectories(formatStorePath);
        }

        generateAutoCompleteXml(commonPageModels);

        generateCategory(commonPageModels);

        generatePages(commonPageModels);

        Set<CategoryInfo> shortPageInfos = generateIndexPage(commonPageModels);

        generateFile(createIndexPageContext(shortPageInfos, commonPageModels), "v2\\index.html");

        generateSitemapXml(commonPageModels);

        copyTemplateResources();

        log.info("End generate task.");
    }

    @SneakyThrows
    private void copyTemplateResources() {
        FileUtils.copyDirectory(new File(generateProperties.getConfigTemplatePath() + File.separator + "css"), new File(pathForStore.toString() + File.separator + "css"));
        FileUtils.copyDirectory(new File(generateProperties.getConfigTemplatePath() + File.separator + "img"), new File(pathForStore.toString() + File.separator + "img"));
        FileUtils.copyDirectory(new File(generateProperties.getConfigTemplatePath() + File.separator + "js"), new File(pathForStore.toString() + File.separator + "js"));
        FileUtils.copyDirectory(new File(generateProperties.getConfigTemplatePath() + File.separator + "assets"), new File(pathForStore.toString() + File.separator + "assets"));
    }

    @Data
    @AllArgsConstructor
    public class ShortPageInfo {
        private String url;
        private String imgUrl;
        private String name;
        private String description;
    }

    @Data
    public static class CategoryModel {
        private String name;
        private List<ShortPageInfo> pages;
    }

    @SneakyThrows
    private void generateCategory(List<Page> commonPageModels) {
        Set<String> categories =
                commonPageModels.stream()
                        .map(this::getCategories)
                        .flatMap((Function<Set<String>, Stream<String>>) Collection::stream)
                        .collect(Collectors.toSet());

        List<CategoryInfo> cats = categories.stream()
                .map(cat -> new CategoryInfo(transliterate(cat) + ".html", cat))
                .collect(Collectors.toList());

        categories.forEach(category ->{
            CategoryModel categoryModel = new CategoryModel();
            categoryModel.setName(category);

            categoryModel.setPages(
                commonPageModels.stream()
                        .filter(page -> getCategories(page).contains(category))
                        .map(page -> new ShortPageInfo(page.getUniqueId() + ".html", page.getPictureUrl(), page.getPayload().get("title").textValue(), page.getPayload().get("description").textValue() ))
                        .collect(Collectors.toList())
            );

            generateFile(createCategoryPageContext(categoryModel, cats), "v2/category.html");
        });
    }

    private   String transliterate(String message) {
        char[] abcCyr = {' ', 'а', 'б', 'в', 'г', 'д', 'е', 'ё', 'ж', 'з', 'и', 'й', 'к', 'л', 'м', 'н', 'о', 'п', 'р', 'с', 'т', 'у', 'ф', 'х', 'ц', 'ч', 'ш', 'щ', 'ъ', 'ы', 'ь', 'э', 'ю', 'я', 'А', 'Б', 'В', 'Г', 'Д', 'Е', 'Ё', 'Ж', 'З', 'И', 'Й', 'К', 'Л', 'М', 'Н', 'О', 'П', 'Р', 'С', 'Т', 'У', 'Ф', 'Х', 'Ц', 'Ч', 'Ш', 'Щ', 'Ъ', 'Ы', 'Ь', 'Э', 'Ю', 'Я', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
        String[] abcLat = {"-", "a", "b", "v", "g", "d", "e", "e", "zh", "z", "i", "y", "k", "l", "m", "n", "o", "p", "r", "s", "t", "u", "f", "h", "ts", "ch", "sh", "sch", "", "i", "", "e", "ju", "ja", "A", "B", "V", "G", "D", "E", "E", "Zh", "Z", "I", "Y", "K", "L", "M", "N", "O", "P", "R", "S", "T", "U", "F", "H", "Ts", "Ch", "Sh", "Sch", "", "I", "", "E", "Ju", "Ja", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < message.length(); i++) {
            for (int x = 0; x < abcCyr.length; x++) {
                if (message.charAt(i) == abcCyr[x]) {
                    builder.append(abcLat[x]);
                }
            }
        }
        return builder.toString();
    }

    private Set<String> getCategories(Page page) {
        Set<String> categories = new HashSet<>();

        Iterator<JsonNode> categoriesIterator = page.getPayload().get("categories").elements();

        while (categoriesIterator.hasNext()) {
            categories.add(categoriesIterator.next().textValue());
        }

        return categories;
    }

    @SneakyThrows
    private void generateAutoCompleteXml(List<Page> commonPageModels) {
        /*
        * <?xml version="1.0" encoding="UTF-8"?>
        <Autocompletions start="0" num="2" total="2">
          <Autocompletion term="asas" type="1" match="1"/>
          <Autocompletion term="Рецепты Шашлыка" type="1" match="1"/>
        </Autocompletions>
        * */
        Set<String> pageTitles =
                commonPageModels.stream().map(page-> page.getPayload().get("header").textValue())
                        .collect(Collectors.toSet());

        Set<String> categories = new HashSet<>();
                commonPageModels.stream()
                        .map(this::getCategories)
                        .flatMap((Function<Set<String>, Stream<String>>) Collection::stream)
                        .collect(Collectors.toSet());

        categories.addAll(pageTitles);
        List<String> totalLinks = categories.stream().limit(2000).collect(Collectors.toList());

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        stringBuilder.append("<Autocompletions start=\"0\" num= \"" + categories.size() + "\" total=\"" + categories.size() + "\">");
        totalLinks.stream().forEach(cat -> stringBuilder.append("<Autocompletion term=\""+ cat.replaceAll("\"", "") + "\" type=\"1\" match=\"1\"/>"));
        stringBuilder.append("</Autocompletions>");

        FileUtils.writeStringToFile(new File(pathForStore.toString() + File.separator + "googleAutoComplete.xml"), stringBuilder.toString());
    }

    @SneakyThrows
    private void generateSitemapXml(List<Page> commonPageModels) {
        WebSitemapGenerator wsg = WebSitemapGenerator.builder("https://www.qas.su", new File(generateProperties.getStorePath()))
                .build();
        commonPageModels.forEach(page -> {
            wsg.addUrl("https://www.qas.su/" + page.getUniqueId() + ".html");
        });

        Set<String> categories =
                commonPageModels.stream()
                        .map(this::getCategories)
                        .flatMap((Function<Set<String>, Stream<String>>) Collection::stream)
                        .map(this::transliterate)
                        .collect(Collectors.toSet());

        categories.forEach(page -> {
            wsg.addUrl("https://www.qas.su/" + page + ".html");
        });


        wsg.write();
    }

    private Set<CategoryInfo> generateIndexPage(List<Page> commonPageModels) {
        return
                commonPageModels.stream()
                        .map(this::getCategories)
                        .flatMap((Function<Set<String>, Stream<String>>) Collection::stream)
                        .map(cat -> new CategoryInfo(transliterate(cat) + ".html", cat))
                        .collect(Collectors.toSet());
    }

    @Data
    @AllArgsConstructor
    public class CategoryInfo {
        private String categoryUrl;
        private String name;
    }

    @Data
    public static class Block {
        private List<CategoryInfo> categoryInfos = new ArrayList<>();
    }

    private Context createIndexPageContext(Set<CategoryInfo> pagesByCategory, List<Page> pages) {
        Context context = new Context();

        List<Block> blocks = new ArrayList<>();
        int i = 0;
        Block block = new Block();
        for (CategoryInfo cat :  new ArrayList<>(pagesByCategory)) {
            if (i > 10) {
                i = 0;
                blocks.add(block);
                block = new Block();
            }
            i++;
            block.categoryInfos.add(cat);
            if (blocks.size() >= 8) {
                break;
            }
        }

        List<ShortPageInfo> latPages = pages.stream().map(page -> new ShortPageInfo(page.getUniqueId() + ".html", page.getPictureUrl(), page.getPayload().get("title").textValue(), page.getPayload().get("description").textValue()))
                .limit(9).collect(Collectors.toList());

        context.setVariable("blocks", blocks);
        context.setVariable("latPages", latPages);
        context.setVariable("countPages", pages.size());
        context.setVariable("countCategories", pagesByCategory.size());
        context.setVariable("uniqueId", "index");
        return context;
    }

    private Context createCategoryPageContext(CategoryModel model, List<CategoryInfo> cats) {
        Context context = new Context();
        context.setVariable("model", model);
        context.setVariable("categories", cats);
        context.setVariable("uniqueId", transliterate(model.name));
        return context;
    }

    @SneakyThrows
    private List<Page> loadPagesSnapshotForGenerate() {
        List<Page> commonPageModels = new ArrayList<>();
        List<Path> listMetaPages = list(new File(generateProperties.getFormatStorePath()).toPath()).filter(path -> path.toString().contains(".json")).collect(Collectors.toList());

        for (Path path : listMetaPages) {
            byte[] fileBytes = readAllBytes(path);
            Page page = objectMapper.readValue(new String(fileBytes, StandardCharsets.UTF_8), Page.class);
            commonPageModels.add(page);
        }

        return commonPageModels;
    }

    private void generatePages(List<Page> commonPageModels) {
        commonPageModels.forEach(commonPageModel -> {
            generateFile(createPageContext(commonPageModel), "v2/page.html");
        });
    }

    @SneakyThrows
    private void generateFile(Context context, String htmlTemplate) {
        Path filePath = Paths.get(pathForStore.toString(), context.getVariable("uniqueId") + ".html");

        if (exists(Paths.get(imagePath.toString(), context.getVariable("uniqueId") + ".jpg"))) {
            FileUtils.copyFile(Paths.get(imagePath.toString(), context.getVariable("uniqueId") + ".jpg").toFile(),
                    Paths.get(pathForStore.toString(), context.getVariable("uniqueId") + ".jpg").toFile());
        }

        deleteIfExists(filePath);
        write(
                filePath,
                templateEngine.process(htmlTemplate, context).getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW);
    }

    private Context createPageContext(Page pageMetaData) {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> result = mapper.convertValue(pageMetaData.getPayload(), new TypeReference<Map<String, Object>>() {});
        result.put("metas", pageMetaData.getMetas());
        result.put("sourceUrl", pageMetaData.getSourceUrl());
        result.put("uniqueId", pageMetaData.getUniqueId());
        result.put("pictureUrl", pageMetaData.getPictureUrl());
        result.put("generatedDate", pageMetaData.getGeneratedDate());

        List<CategoryInfo> categories =
                getCategories(pageMetaData).stream()
                .map(cat -> new CategoryInfo(transliterate(cat) + ".html", cat))
                        .collect(Collectors.toList());

        result.put("categoriesWithUrls", categories);

        return new Context(Locale.getDefault(), result);
    }
}
