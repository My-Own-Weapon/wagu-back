package com.chimaera.wagubook.repository.store;

import com.chimaera.wagubook.entity.Store;

import java.util.List;

public interface StoreRepositoryCustom {
    List<Store> searchStores(String keyword); // 가게 검색
}