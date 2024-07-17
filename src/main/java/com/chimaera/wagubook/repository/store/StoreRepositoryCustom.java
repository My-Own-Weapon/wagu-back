package com.chimaera.wagubook.repository.store;

import com.chimaera.wagubook.entity.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StoreRepositoryCustom {
    Page<Store> searchStores(String keyword, Pageable pageable); // 가게 검색

    List<Store> findAllByScreen(String left, String right, String up, String down); // 화면에 들어오는 가게 검색


}
