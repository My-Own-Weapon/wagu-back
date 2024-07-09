package com.chimaera.wagubook.dto;

import com.chimaera.wagubook.entity.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StorePostResponse {
    private Long postId;
    private String memberUsername;
    private String storeName;
    private String postMainMenu;
    private MenuImage menuImage;
    private int menuPrice;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    public StorePostResponse(Post post, Menu menu) {
        this.postId = post.getId();
        this.memberUsername = post.getMember().getUsername();
        this.storeName = post.getStore().getStoreName();
        this.postMainMenu = post.getPostMainMenu();
        this.menuImage = menu.getMenuImage();
        this.menuPrice = menu.getMenuPrice();
        this.createdDate = post.getCreateDate();
        this.updatedDate = post.getUpdateDate();
    }
}
