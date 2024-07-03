package com.chimaera.wagubook.dto;

import com.chimaera.wagubook.entity.Store;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class StoreResponse {
    private String name;
    private String address;
    private Long storeId;
    private float posx;
    private float posy;

    public StoreResponse(Store store){
        this.posx = store.getStoreLocation().getPosx();
        this.posy = store.getStoreLocation().getPosy();
        this.address = store.getStoreLocation().getAddress();
        this.name = store.getStoreName();
        this.storeId = store.getId();
    }
}
