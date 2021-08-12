package ru.translater.format.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Rule {
    private String fieldName;
    private String type;
    private String isTranslated;
    private List<Step> steps = new ArrayList<>();
}
