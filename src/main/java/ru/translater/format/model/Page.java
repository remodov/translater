package ru.translater.format.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import ru.translater.common.model.Meta;

import java.util.ArrayList;
import java.util.List;

@Data
public class Page {
    private String sourceUrl;
    private String uniqueId;
    private String generatedDate;
    //TODO move to payload
    private String pictureUrl;
    private List<Meta> metas = new ArrayList<>();

    private JsonNode payload;
}
