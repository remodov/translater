package com.translater.generate.service

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.redfin.sitemapgenerator.WebSitemapGenerator
import com.translater.common.model.ExecutionTask
import com.translater.format.model.Page
import com.translater.generate.model.Block
import com.translater.generate.model.CategoryInfo
import com.translater.generate.model.CategoryModel
import com.translater.generate.model.ShortPageInfo
import com.translater.generate.properties.GenerateProperties
import com.translater.translate.LanguageProperties
import mu.KLogging
import org.apache.commons.io.FileUtils
import org.springframework.stereotype.Service
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.*
import java.util.function.Consumer
import java.util.stream.Collectors
import javax.annotation.PostConstruct

@Service
class GenerateTaskService(
    private val generateProperties: GenerateProperties,
    val templateEngine: TemplateEngine,
    val objectMapper: ObjectMapper,
    val languageProperties: LanguageProperties
) : ExecutionTask {
    private lateinit var pathForStore: Path
    private lateinit var formatStorePath: Path
    private lateinit var imagePath: Path

    companion object : KLogging()


    @PostConstruct
    fun init() {
        pathForStore = Paths.get(generateProperties.storePath)
        formatStorePath = Paths.get(generateProperties.formatStorePath)
        imagePath = Paths.get(generateProperties.imagePath)
    }

    override fun start() {
        languageProperties.languages.values
            .forEach { generateLingualSitePart(it) }
    }


    private fun generateLingualSitePart(language: String) {
        logger.info("Start generate task for lingual: $language")

        val commonPageModels: List<Page> = loadPagesSnapshotForGenerate(language)

        if (!Files.exists(pathForStore)) {
            Files.createDirectories(pathForStore)
        }

        if (!Files.exists(formatStorePath)) {
            Files.createDirectories(formatStorePath)
        }

        checkLangDirectory(language)

        generateAutoCompleteXml(commonPageModels)

        generateCategory(commonPageModels)

        generatePages(commonPageModels)

        val shortPageInfos: Set<CategoryInfo> = generateIndexPage(commonPageModels)

        generateFile(createIndexPageContext(shortPageInfos, commonPageModels), "v2\\index.html")

        generateSitemapXml(commonPageModels)

        copyTemplateResources()

        logger.info("End generate task for language: $language")
    }

    private fun checkLangDirectory(language: String) {
        val tempDirectory = File("$formatStorePath${File.separator}$language")
        if (tempDirectory.exists()) {
            return
        }
        tempDirectory.mkdir()
    }

    private fun generateIndexPage(commonPageModels: List<Page>): Set<CategoryInfo> {
        return commonPageModels
            .map { page: Page -> getCategories(page) }
            .flatten()
            .map { CategoryInfo("${transliterate(it)}.html", it) }
            .toSet()
    }

    private fun generatePages(commonPageModels: List<Page>) {
        commonPageModels.forEach(Consumer { commonPageModel: Page ->
            generateFile(
                createPageContext(commonPageModel),
                "v2/page.html"
            )
        })
    }

    private fun createPageContext(pageMetaData: Page): Context {
        val mapper = ObjectMapper()
        val result: MutableMap<String, Any> =
            mapper.convertValue(pageMetaData.payload, object : TypeReference<MutableMap<String, Any>>() {})
        result["metas"] = pageMetaData.metas
        result["sourceUrl"] = pageMetaData.sourceUrl ?: ""
        result["uniqueId"] = pageMetaData.uniqueId ?: ""
        result["pictureUrl"] = pageMetaData.pictureUrl ?: ""
        result["generatedDate"] = pageMetaData.generatedDate ?: ""

        val categories = getCategories(pageMetaData)
            .map { CategoryInfo("${transliterate(it)}.html", it) }
        result["categoriesWithUrls"] = categories

        return Context(Locale.getDefault(), result)
    }

    private fun loadPagesSnapshotForGenerate(language: String): List<Page> {
        val commonPageModels: MutableList<Page> = ArrayList()
        Files.list(File("${generateProperties.formatStorePath}${File.separator}$language").toPath())
            .filter { path: Path -> path.toString().contains(".json") }
            .forEach { path ->
                val fileBytes = Files.readAllBytes(path)
                val page = objectMapper.readValue(String(fileBytes, StandardCharsets.UTF_8), Page::class.java)
                commonPageModels.add(page)
            }
        return commonPageModels
    }

    private fun generateAutoCompleteXml(commonPageModels: List<Page>) {
        /*
        * <?xml version="1.0" encoding="UTF-8"?>
        <Autocompletions start="0" num="2" total="2">
          <Autocompletion term="asas" type="1" match="1"/>
          <Autocompletion term="Рецепты Шашлыка" type="1" match="1"/>
        </Autocompletions>
        * */
        val categories: MutableSet<String> = commonPageModels
            .map { page: Page -> page.payload?.get("header")?.textValue() ?: "" }
            .filter { it != "" }
            .let { pageTitles ->
                val categories: MutableSet<String> = commonPageModels
                    .map { page -> getCategories(page) }
                    .flatten().toMutableSet()

                categories.addAll(pageTitles)
                return@let categories
            }


        val totalLinks = categories.take(2000)
        val stringBuilder = StringBuilder().apply {
            append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
                .append("<Autocompletions start=\"0\" num= \"" + categories.size + "\" total=\"" + categories.size + "\">")

        }
        totalLinks.forEach { cat: String ->
            stringBuilder.append(
                "<Autocompletion term=\"" + cat.replace("\"".toRegex(), "") + "\" type=\"1\" match=\"1\"/>"
            )
        }
        stringBuilder.append("</Autocompletions>")
        FileUtils.writeStringToFile(
            File(pathForStore.toString() + File.separator + "googleAutoComplete.xml"),
            stringBuilder.toString()
        )
    }

    fun generateSitemapXml(commonPageModels: List<Page>) {
        WebSitemapGenerator.builder("https://www.qas.su", File(generateProperties.storePath))
            .build().let { wsg ->
                commonPageModels.forEach(Consumer { page: Page ->
                    wsg.addUrl("https://www.qas.su/${page.uniqueId}.html")
                })

                commonPageModels
                    .map { page -> getCategories(page) }
                    .flatten()
                    .map { message -> transliterate(message) }.let { categories ->
                        categories.forEach(Consumer { page: String? -> wsg.addUrl("https://www.qas.su/$page.html") })
                    }

                wsg.write()
            }
    }

    fun copyTemplateResources() {
        FileUtils.copyDirectory(
            File("${generateProperties.configTemplatePath}${File.separator}css"),
            File("$pathForStore${File.separator}css")
        )
        FileUtils.copyDirectory(
            File("${generateProperties.configTemplatePath}${File.separator}img"),
            File("$pathForStore${File.separator}img")
        )
        FileUtils.copyDirectory(
            File("${generateProperties.configTemplatePath}${File.separator}js"),
            File("$pathForStore${File.separator}js")
        )
        FileUtils.copyDirectory(
            File("${generateProperties.configTemplatePath}${File.separator}assets"),
            File("$pathForStore${File.separator}assets")
        )
    }

    fun createIndexPageContext(pagesByCategory: Set<CategoryInfo>, pages: List<Page>): Context {
        //TODO: refactoring
        val context = Context()
        val blocks: MutableList<Block> = ArrayList()
        var i = 0
        var block = Block()
        for (cat in pagesByCategory) {
            if (i > 10) {
                i = 0
                blocks.add(block)
                block = Block()
            }
            i++
            block.categoryInfos.add(cat)
            if (blocks.size >= 8) {
                break
            }
        }
        val latPages = pages.stream().map { page: Page ->
            ShortPageInfo(
                "${page.uniqueId}.html",
                page.pictureUrl,
                page.payload?.get("title")?.textValue(),
                page.payload?.get("description")?.textValue()
            )
        }
            .limit(9).collect(Collectors.toList())
        context.setVariable("blocks", blocks)
        context.setVariable("latPages", latPages)
        context.setVariable("countPages", pages.size)
        context.setVariable("countCategories", pagesByCategory.size)
        context.setVariable("uniqueId", "index")
        return context
    }

    fun generateCategory(commonPageModels: List<Page>) {
        val categories: Set<String> = commonPageModels
            .map { getCategories(it) }
            .flatten()
            .toSet()

        val cats: List<CategoryInfo> = categories
            .map { cat: String ->
                CategoryInfo("${transliterate(cat)}.html", cat)
            }
        categories.forEach(Consumer { category: String? ->
            val categoryModel = CategoryModel()
            categoryModel.name = category
            categoryModel.pages =
                commonPageModels.filter { page: Page -> getCategories(page).contains(category) }
                    .map { page: Page ->
                        ShortPageInfo(
                            page.uniqueId.toString() + ".html",
                            page.pictureUrl,
                            page.payload?.get("title")?.textValue(),
                            page.payload?.get("description")?.textValue()
                        )
                    }
            generateFile(createCategoryPageContext(categoryModel, cats), "v2/category.html")
        })

    }

    private fun generateFile(context: Context, htmlTemplate: String) {
        val filePath = Paths.get(pathForStore.toString(), context.getVariable("uniqueId").toString() + ".html")
        if (Files.exists(Paths.get(imagePath.toString(), context.getVariable("uniqueId").toString() + ".jpg"))) {
            FileUtils.copyFile(
                Paths.get(imagePath.toString(), context.getVariable("uniqueId").toString() + ".jpg").toFile(),
                Paths.get(pathForStore.toString(), context.getVariable("uniqueId").toString() + ".jpg").toFile()
            )
        }
        Files.deleteIfExists(filePath)
        Files.write(
            filePath,
            templateEngine.process(htmlTemplate, context).toByteArray(StandardCharsets.UTF_8),
            StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW
        )
    }

    private fun createCategoryPageContext(model: CategoryModel, cats: List<CategoryInfo>): Context {
        return Context().apply {
            this.setVariable("model", model)
            this.setVariable("categories", cats)
            this.setVariable("uniqueId", transliterate(model.name!!))
        }
    }

    private fun getCategories(page: Page): Set<String> {
        val categories: MutableSet<String> = HashSet()
        val categoriesIterator: Iterator<JsonNode>? = page.payload?.get("categories")?.elements()?.iterator()
        if (categoriesIterator != null) {
            while (categoriesIterator.hasNext()) {
                categories.add(categoriesIterator.next().textValue())
            }
        }
        return categories
    }

    private fun transliterate(message: String): String? {
        val abcCyr = charArrayOf(
            ' ',
            'а',
            'б',
            'в',
            'г',
            'д',
            'е',
            'ё',
            'ж',
            'з',
            'и',
            'й',
            'к',
            'л',
            'м',
            'н',
            'о',
            'п',
            'р',
            'с',
            'т',
            'у',
            'ф',
            'х',
            'ц',
            'ч',
            'ш',
            'щ',
            'ъ',
            'ы',
            'ь',
            'э',
            'ю',
            'я',
            'А',
            'Б',
            'В',
            'Г',
            'Д',
            'Е',
            'Ё',
            'Ж',
            'З',
            'И',
            'Й',
            'К',
            'Л',
            'М',
            'Н',
            'О',
            'П',
            'Р',
            'С',
            'Т',
            'У',
            'Ф',
            'Х',
            'Ц',
            'Ч',
            'Ш',
            'Щ',
            'Ъ',
            'Ы',
            'Ь',
            'Э',
            'Ю',
            'Я',
            'a',
            'b',
            'c',
            'd',
            'e',
            'f',
            'g',
            'h',
            'i',
            'j',
            'k',
            'l',
            'm',
            'n',
            'o',
            'p',
            'q',
            'r',
            's',
            't',
            'u',
            'v',
            'w',
            'x',
            'y',
            'z',
            'A',
            'B',
            'C',
            'D',
            'E',
            'F',
            'G',
            'H',
            'I',
            'J',
            'K',
            'L',
            'M',
            'N',
            'O',
            'P',
            'Q',
            'R',
            'S',
            'T',
            'U',
            'V',
            'W',
            'X',
            'Y',
            'Z'
        )
        val abcLat = arrayOf(
            "-",
            "a",
            "b",
            "v",
            "g",
            "d",
            "e",
            "e",
            "zh",
            "z",
            "i",
            "y",
            "k",
            "l",
            "m",
            "n",
            "o",
            "p",
            "r",
            "s",
            "t",
            "u",
            "f",
            "h",
            "ts",
            "ch",
            "sh",
            "sch",
            "",
            "i",
            "",
            "e",
            "ju",
            "ja",
            "A",
            "B",
            "V",
            "G",
            "D",
            "E",
            "E",
            "Zh",
            "Z",
            "I",
            "Y",
            "K",
            "L",
            "M",
            "N",
            "O",
            "P",
            "R",
            "S",
            "T",
            "U",
            "F",
            "H",
            "Ts",
            "Ch",
            "Sh",
            "Sch",
            "",
            "I",
            "",
            "E",
            "Ju",
            "Ja",
            "a",
            "b",
            "c",
            "d",
            "e",
            "f",
            "g",
            "h",
            "i",
            "j",
            "k",
            "l",
            "m",
            "n",
            "o",
            "p",
            "q",
            "r",
            "s",
            "t",
            "u",
            "v",
            "w",
            "x",
            "y",
            "z",
            "A",
            "B",
            "C",
            "D",
            "E",
            "F",
            "G",
            "H",
            "I",
            "J",
            "K",
            "L",
            "M",
            "N",
            "O",
            "P",
            "Q",
            "R",
            "S",
            "T",
            "U",
            "V",
            "W",
            "X",
            "Y",
            "Z"
        )
        val builder = StringBuilder()
        for (element in message) {
            for (x in abcCyr.indices) {
                if (element == abcCyr[x]) {
                    builder.append(abcLat[x])
                }
            }
        }
        return builder.toString()
    }
}