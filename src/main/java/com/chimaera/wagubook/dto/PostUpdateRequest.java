package com.chimaera.wagubook.dto;

import com.chimaera.wagubook.entity.Category;
import com.chimaera.wagubook.entity.Location;
import com.chimaera.wagubook.entity.Permission;
import lombok.Data;

import java.util.List;

@Data
public class PostUpdateRequest {
    private String storeName;
    private Location storeLocation;
    private Category postCategory;
    private String postMainMenu;
    private Permission permission;
    private boolean isAuto;
    private List<PostUpdateRequest.MenuUpdateRequest> menus;

    @Data
    public static class MenuUpdateRequest {
        private Long menuId;
        private String menuName;
        private int menuPrice;
        private String menuContent;
    }
}
