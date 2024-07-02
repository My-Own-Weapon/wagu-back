package com.chimaera.wagubook.dto;

import com.chimaera.wagubook.entity.Store;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class StoreResponse {
    private String name;
    private float posx;
    private float posy;

    public StoreResponse(Store store){
        this.posx = store.getStoreLocation().getPosx();
        this.posy = store.getStoreLocation().getPosy();
        this.name = store.getStoreName();
    }
}
