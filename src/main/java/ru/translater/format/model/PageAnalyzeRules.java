package ru.translater.format.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PageAnalyzeRules {
    private List<Rule> rules = new ArrayList<>();
}
