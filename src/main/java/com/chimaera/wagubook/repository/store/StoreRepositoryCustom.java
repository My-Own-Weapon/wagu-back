package com.chimaera.wagubook.repository.store;

import com.chimaera.wagubook.entity.Store;

import java.util.List;

public interface StoreRepositoryCustom {
    List<Store> searchStores(String keyword); // 가게 검색

    List<Store> findAllByScreen(String left, String right, String up, String down); // 화면에 들어오는 가게 검색


}
