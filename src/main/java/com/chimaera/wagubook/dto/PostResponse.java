package com.chimaera.wagubook.dto;

import com.chimaera.wagubook.entity.*;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class PostResponse {
    private Long postId;
    private String memberUsername;
    private String storeName;
    private Location storeLocation;
    private String postMainMenu;
    private Category postCategory;
    private Permission permission;
    private boolean isAuto;
    private boolean isFinished;
    private List<MenuResponse> menus;

    public PostResponse (Post post) {
        this.postId = post.getId();
        this.memberUsername = post.getMember().getUsername();
        this.storeName = post.getStore().getStoreName();
        this.storeLocation = post.getStore().getStoreLocation();
        this.postCategory = post.getCategory();
        this.postMainMenu = post.getPostMainMenu();
        this.permission = post.getPermission();
        this.isAuto = post.isAuto();
        this.isFinished = post.isFinished();
        this.menus = post.getMenus().stream()
                .map(menu -> new MenuResponse(menu))
                .collect(Collectors.toList());
    }

    @Data
    public static class MenuResponse {
        private Long menuId;
        private MenuImage menuImage;
        private String menuName;
        private int menuPrice;
        private String menuContent;

        public MenuResponse(Menu menu) {
            this.menuId = menu.getId();
            this.menuImage = menu.getMenuImage();
            this.menuName = menu.getMenuName();
            this.menuPrice = menu.getMenuPrice();
            this.menuContent = menu.getMenuContent();
        }
    }
}
