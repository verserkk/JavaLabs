package com.example.animeservice.dto;

import lombok.Data;

@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
@Data
public class VisitDto {
    private String url;
    private long visitCount;
}