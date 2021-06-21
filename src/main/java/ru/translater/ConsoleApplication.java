package ru.translater;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.translater.translation.TranslationClient;


@Slf4j
@SpringBootApplication
public class ConsoleApplication implements CommandLineRunner {
    @Autowired
    private TranslationClient translationClient;

    public static void main(String[] args) {
        log.info("STARTING THE APPLICATION");
        SpringApplication.run(ConsoleApplication.class, args);
        log.info("APPLICATION FINISHED");
    }

    @Override
    public void run(String... args) {
        log.info("EXECUTING : command line runner");

        for (int i = 0; i < args.length; ++i) {
            log.info("args[{}]: {}", i, args[i]);
        }
    }
}
