package com.chimaera.wagubook.dto;


import com.chimaera.wagubook.entity.Location;
import lombok.Data;

import java.util.List;


@Data
public class PostRequest {
    private String postMainMenu;
    private String postImage;
    private String postContent;
    private boolean isAuto;
    private String storeName;
    private Location storeLocation;
    private List<MenuRequest> menus;

    @Data
    public static class MenuRequest {
        private String menuName;
        private int menuPrice;
        private String categoryName;
    }
}
