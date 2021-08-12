package ru.translater.collect;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.translater.common.ExecutionTask;

@RequiredArgsConstructor
@Slf4j
@Component
public class CollectTask implements ExecutionTask {
    private final CollectProperties collectProperties;

    @SneakyThrows
    @Override
    public void start() {
        log.info("Start collection task.");

        CrawlConfig config = new CrawlConfig();
        config.setCrawlStorageFolder(collectProperties.getStorePath());

        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);

        collectProperties.getSeedUrls().forEach(controller::addSeed);

        CrawlController.WebCrawlerFactory<Crawler> factory = () -> new Crawler(collectProperties);

        controller.start(factory, collectProperties.getNumberOfCrawlers());

        log.info("End collection task.");
    }
}
