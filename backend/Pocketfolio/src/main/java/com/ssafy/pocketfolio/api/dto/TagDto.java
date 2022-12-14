package com.ssafy.pocketfolio.api.dto;

import com.ssafy.pocketfolio.db.entity.Portfolio;
import lombok.Builder;

@Builder
public class TagDto {
    private Long tagSeq;
    private String name;
    private Portfolio portfolio;
}
