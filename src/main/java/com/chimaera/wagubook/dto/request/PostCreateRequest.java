package com.chimaera.wagubook.dto.request;

import com.chimaera.wagubook.entity.Category;
import com.chimaera.wagubook.entity.Location;
import com.chimaera.wagubook.entity.Permission;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class PostCreateRequest {
    @NotNull(message = "식당 이름은 필수 값입니다.")
    private String storeName;

    @NotNull(message = "식당 위치는 필수 값입니다.")
    private Location storeLocation;

    @NotNull(message = "포스트 카테고리는 필수 값입니다.")
    private Category postCategory;

    @NotNull(message = "포스트 메인 메뉴는 필수 값입니다.")
    private String postMainMenu;

    @NotNull(message = "포스트 권한 설정은 필수 값입니다.")
    private Permission permission;

    @NotNull(message = "포스트 자동화 여부는 필수 값입니다.")
    private boolean isAuto;

    @Valid
    @NotNull(message = "포스트 당 하나의 메뉴는 필수적으로 작성해야 합니다.")
    private List<MenuCreateRequest> menus;

    @Data
    public static class MenuCreateRequest {
        @NotNull(message = "메뉴 이름은 필수 값입니다.")
        private String menuName;

        @NotNull(message = "메뉴 가격은 필수 값입니다.")
        private int menuPrice;

        @Size(max = 5000, message = "메뉴 설명은 최대 5000자까지 가능합니다.")
        private String menuContent;
    }
}
