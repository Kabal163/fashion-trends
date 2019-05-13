package com.epam.fashion.trends.api.entity;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class Message {

    private Integer creationTime;
    private Integer userId;
    private String content;
    private Set<String> hashtags = new HashSet<>();

    public void addTag(String tag) {
        this.hashtags.add(tag);
    }
}
