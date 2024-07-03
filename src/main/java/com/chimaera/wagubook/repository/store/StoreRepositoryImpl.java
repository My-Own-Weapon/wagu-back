package com.chimaera.wagubook.repository.store;

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

    @Override
    public List<Store> findAllByScreen(String left, String right, String up, String down){
        QStore store = QStore.store;
        return queryFactory
                .selectFrom(store)
                .where(store.storeLocation.posx.between(Double.parseDouble(left), Double.parseDouble(right))
                        .and(store.storeLocation.posy.between(Double.parseDouble(up), Double.parseDouble(down))))
                .fetch();
    }
}
