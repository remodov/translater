package ru.translater.collect;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import ru.translater.format.ImageUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

@Slf4j
public class Crawler extends WebCrawler {
    private final CollectProperties collectProperties;

    public Crawler(CollectProperties collectProperties){
        this.collectProperties = collectProperties;
    }
    //TODO move to properties
    private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|gif|jpg|png|mp3|mp4|zip|gz))$");

    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        String href = url.getURL().toLowerCase();
        return !FILTERS.matcher(href).matches()
                && isContainsAllFilters(href)
                && isContainsNoAllFilters(href);
    }

    private boolean isContainsNoAllFilters(String href) {
        for (String filterUrl : collectProperties.getNotFilterUrls()) {
            if (href.contains(filterUrl)) {
                return false;
            }
        }

        return true;
    }

    private boolean isContainsAllFilters(String href) {
        for (String filterUrl : collectProperties.getFilterUrls()) {
            if (!href.contains(filterUrl)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void visit(Page page) {
        String url = page.getWebURL().getURL();

        System.out.println("URL: " + url);

        if (!isContainsAllFilters(url)){
            System.out.println("URL does not contains all filters: " + url);
            return;
        }

        if (page.getParseData() instanceof HtmlParseData) {
            HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
            String crawlStorageFolder = myController.getConfig().getCrawlStorageFolder();

            try {
                Path path = Paths.get(crawlStorageFolder,
                        page.getWebURL().getURL()
                                .replaceAll("https://www.", "")
                                .replaceAll("https://", "")
                                .replaceAll("\\.", "-")
                                .replaceAll("/", "-") );


                Files.write(Paths.get(path + ".html"), htmlParseData.getHtml().getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                log.error("Error when parsing page: " + url + " error message: " + e.getLocalizedMessage(), e);
            }
        }
    }
}
