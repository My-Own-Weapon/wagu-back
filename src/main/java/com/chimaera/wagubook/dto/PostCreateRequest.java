package com.chimaera.wagubook.dto;

import com.chimaera.wagubook.entity.Category;
import com.chimaera.wagubook.entity.Location;
import com.chimaera.wagubook.entity.Permission;
import lombok.Data;

import java.util.List;

@Data
public class PostCreateRequest {
    private String storeName;
    private Location storeLocation;
    private Category postCategory;
    private String postMainMenu;
    private Permission permission;
    private boolean isAuto;
    private List<MenuCreateRequest> menus;

    @Data
    public static class MenuCreateRequest {
        private String menuName;
        private int menuPrice;
        private String menuContent;
    }
}
