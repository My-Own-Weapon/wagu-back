package com.chimaera.wagubook.repository;

import com.chimaera.wagubook.entity.QStore;
import com.chimaera.wagubook.entity.Store;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
@RequiredArgsConstructor
public class StoreRepositoryImpl implements StoreRepositoryCustom{
    private final JPAQueryFactory queryFactory;

    @Override
    public List<Store> searchStores(String keyword) {
        QStore store = QStore.store;
        return queryFactory
                .selectFrom(store)
                .where(store.storeName.containsIgnoreCase(keyword)) // 대소문자 구분 없이 검색
                .fetch();
    }
}
