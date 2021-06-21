package ru.translater;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

public class GreatBritishChefsComCrawler extends WebCrawler {
    private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|gif|jpg"
            + "|png|mp3|mp4|zip|gz))$");

    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        String href = url.getURL().toLowerCase();
        return !FILTERS.matcher(href).matches()
                && href.startsWith("https://www.greatbritishchefs.com/recipes/");
    }

    @Override
    public void visit(Page page) {
        String url = page.getWebURL().getURL();
        System.out.println("URL: " + url);

        if (page.getParseData() instanceof HtmlParseData) {
            HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
            String html = htmlParseData.getHtml();
            String crawlStorageFolder = myController.getConfig().getCrawlStorageFolder();


            /// extract data
            Document document = Jsoup.parse(html);
            String ingredientsText = "";
            String methodText = "";

            Elements ingredients = document.getElementsByClass("Recipe__Ingredients Ingredients");
            if (ingredients != null && ingredients.size() == 1) {
                ingredientsText = ingredients.get(0).html();
            }

            Elements method = document.getElementsByClass("Recipe__Method Method");
            if (method != null && method.size() == 1) {
                methodText = method.get(0).html();
            }


            try {
                Path path = Paths.get(crawlStorageFolder,
                        page.getWebURL().getURL()
                                .replaceAll("https://www.", "")
                                .replaceAll("\\.", "-")
                                .replaceAll("/", "-") + ".html");

                String htmlResult =
                        "<html>" +
                                "<body>" +
                                "<span>" +
                                    ingredientsText +
                                "</span>" +
                                "<span>" +
                                    methodText +
                                "</span>" +
                                "</body>" +
                                "</html>";

                Files.write(path, htmlResult.getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
