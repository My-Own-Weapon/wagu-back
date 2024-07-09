package com.chimaera.wagubook.dto;

import com.chimaera.wagubook.entity.Store;
import lombok.Data;

@Data
public class StoreResponse {
    private String storeName;
    private String storeAddress;
    private Long storeId;
    private double posx;
    private double posy;

    public StoreResponse(Store store){
        this.posx = store.getStoreLocation().getPosx();
        this.posy = store.getStoreLocation().getPosy();
        this.storeAddress = store.getStoreLocation().getAddress();
        this.storeName = store.getStoreName();
        this.storeId = store.getId();
    }
}
