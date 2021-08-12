package ru.translater.format;

import com.google.common.io.Files;
import org.apache.logging.log4j.util.Strings;
import org.json.JSONException;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.Customization;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.comparator.CustomComparator;
import ru.translater.common.configuration.CommonConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FormatTaskTest {



    public void test() throws IOException, JSONException {
        File tempDir = Files.createTempDir();
        String formatPath = getClass().getClassLoader().getResource("format").getPath();
        FormatProperties properties = new FormatProperties();
        properties.setFormatTemplatePath(formatPath + File.separatorChar + "format-1.json");
        properties.setAnalyzePath(formatPath);
        properties.setStorePath(tempDir.getPath());

        FormatTask formatTask = new FormatTask(properties, new CommonConfiguration().objectMapper());
        formatTask.start();

        String pictureName = tempDir.list()[0];
        String fileName = tempDir.list()[1];
        assertEquals( "greatbritishchefs-com-recipes-aioli-recipe.jpg", pictureName);
        assertEquals( "greatbritishchefs-com-recipes-aioli-recipe.json", fileName);

        String actualFile = Strings.join(Files.readLines(new File(tempDir + File.separator + fileName), StandardCharsets.UTF_8), ' ');
        String expectedFile = Strings.join(Files.readLines(new File(formatPath + File.separator + "greatbritishchefs-com-recipes-aioli-recipe.json"), StandardCharsets.UTF_8), ' ');

//        JSONAssert.assertEquals(expectedFile, actualFile, new CustomComparator(JSONCompareMode.LENIENT,
//                new Customization("generatedDate", (ct1, ct2) -> true)));
    }

}