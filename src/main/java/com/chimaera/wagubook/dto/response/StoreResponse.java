package com.chimaera.wagubook.dto.response;

import com.chimaera.wagubook.entity.Store;
import lombok.Data;

@Data
public class StoreResponse {
    private String storeName;
    private String storeAddress;
    private Long storeId;
    private double posx;
    private double posy;
    // store에서 live 중인지에 대한 정보
    private boolean isLiveStore;

    public StoreResponse(Store store, boolean isLiveStore){
        this.posx = store.getStoreLocation().getPosx();
        this.posy = store.getStoreLocation().getPosy();
        this.storeAddress = store.getStoreLocation().getAddress();
        this.storeName = store.getStoreName();
        this.storeId = store.getId();
        this.isLiveStore = isLiveStore;
    }
}
