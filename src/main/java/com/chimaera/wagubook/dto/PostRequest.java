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
    private String storeName; // New field for store name
    private Location storeLocation; // New field for store location
    private List<MenuRequest> menus; // New field for menus

    @Data
    public static class MenuRequest {
        private String menuName;
        private int menuPrice;
        private String categoryName;
    }
}
