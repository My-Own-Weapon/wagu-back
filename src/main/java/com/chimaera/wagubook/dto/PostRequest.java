package com.chimaera.wagubook.dto;

import com.chimaera.wagubook.entity.Category;
import com.chimaera.wagubook.entity.Location;
import com.chimaera.wagubook.entity.Permission;
import lombok.Data;

import java.util.List;

@Data
public class PostRequest {
    private String storeName;
    private Location storeLocation;
    private Category postCategory;
    private String postMainMenu;
    private Permission permission;
    private boolean isAuto;
    private List<MenuRequest> menus;

    @Data
    public static class MenuRequest {
        private String menuName;
        private int menuPrice;
        private String menuContent;
    }
}
