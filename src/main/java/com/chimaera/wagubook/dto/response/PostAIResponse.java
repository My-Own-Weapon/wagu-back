package com.chimaera.wagubook.dto.response;

import lombok.Data;

@Data
public class PostAIResponse {
    private String menuContent;

    public PostAIResponse(String menuContent) {
        this.menuContent = menuContent;
    }
}
