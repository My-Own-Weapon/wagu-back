package com.chimaera.wagubook.dto;

import lombok.Data;

@Data
public class PostResponse {
    private Long id; // post_id
    private String postMainMenu;
    private String postImage;
    private String postContent;
    private boolean isAuto;

    public PostResponse(Long id, String postMainMenu, String postImage, String postContent, boolean isAuto) {
        this.id = id;
        this.postMainMenu = postMainMenu;
        this.postImage = postImage;
        this.postContent = postContent;
        this.isAuto = isAuto;
    }
}
