package com.chimaera.wagubook.dto;

import lombok.Data;

@Data
public class PostRequest {
    private String postMainMenu;
    private String postImage;
    private String postContent;
    private boolean isAuto;
}
