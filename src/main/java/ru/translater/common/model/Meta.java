package ru.translater.common.model;

import lombok.Data;
import org.jsoup.internal.StringUtil;

@Data
public class Meta {
    private String name;
    private String property;
    private String content;

    public boolean isNameExists() {
        return StringUtil.isBlank(property) && !StringUtil.isBlank(name);
    }
}
