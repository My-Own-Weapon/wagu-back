package com.chimaera.wagubook.repository.store;

import com.chimaera.wagubook.entity.QStore;
import com.chimaera.wagubook.entity.Store;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
@RequiredArgsConstructor
public class StoreRepositoryImpl implements StoreRepositoryCustom{
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Store> searchStores(String keyword, Pageable pageable) {
        QStore store = QStore.store;

        List<Store> stores = queryFactory.selectFrom(store)
                .where(store.storeName.containsIgnoreCase(keyword))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory.selectFrom(store)
                .where(store.storeName.containsIgnoreCase(keyword))
                .fetchCount();

        return new PageImpl<>(stores, pageable, total);
    }

    @Override
    public List<Store> findAllByScreen(String left, String right, String up, String down){
        QStore store = QStore.store;
        return queryFactory
                .selectFrom(store)
                .where(store.storeLocation.posx.between(Double.parseDouble(left), Double.parseDouble(right))
                        .and(store.storeLocation.posy.between(Double.parseDouble(down), Double.parseDouble(up))))
                .fetch();
    }
}
