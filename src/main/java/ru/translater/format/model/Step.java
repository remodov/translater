package ru.translater.format.model;

import lombok.Data;

@Data
public class Step {
    private Integer order;
    private String searchValue;
    private String searchType;
    //list, element
    private String extractor;
}