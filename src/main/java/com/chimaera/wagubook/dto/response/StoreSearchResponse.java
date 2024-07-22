package com.chimaera.wagubook.dto.response;

import com.chimaera.wagubook.entity.MenuImage;
import com.chimaera.wagubook.entity.Store;
import lombok.Data;

@Data
public class StoreSearchResponse {
    private Long storeId;
    private String storeName;
    private MenuImage menuImage;
    private int postCount;
    // TODO: store 에 가장 최근에 등록된 메인메뉴 1개를 가져옴.
    private String menuName;




    public StoreSearchResponse(Store store) {
        this.storeId = store.getId();
        this.storeName = store.getStoreName();
        this.postCount = store.getPosts().size();
        this.menuName = store.getMenus().get(0).getMenuName();
        // 가장 처음에 달린 Menu Image 가져오기
        if (store.getMenus() != null) {
            this.menuImage = store.getMenus().get(0).getMenuImage();
        }
    }
}
