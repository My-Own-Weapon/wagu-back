package com.chimaera.wagubook.repository.post;


import com.chimaera.wagubook.entity.*;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Post> searchPostsByMemberIdAndStoreName(Long memberId, String keyword, Pageable pageable) {
        QPost post = QPost.post;
        QStore store = QStore.store;

        List<Post> posts = queryFactory.selectFrom(post)
                .leftJoin(post.store, store)
                .where(post.member.id.eq(memberId)
                        .and(store.storeName.containsIgnoreCase(keyword)))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory.selectFrom(post)
                .leftJoin(post.store, store)
                .where(post.member.id.eq(memberId)
                        .and(store.storeName.containsIgnoreCase(keyword)))
                .fetchCount();

        return new PageImpl<>(posts, pageable, total);
    }

    @Override
    public List<Post> findByStoreIdAndPage(Long memberId, Long storeId, int page, int size){
        QPost post = QPost.post;
        QFollow follow = QFollow.follow;

        return queryFactory
                .selectFrom(post)
                .where(post.store.id.eq(storeId)
                .and(post.permission.eq(Permission.PUBLIC)
                        .or(post.permission.eq(Permission.FRIENDS)
                                .and(post.member.id
                                        .in(queryFactory
                                                .select(follow.toMember.id)
                                                .from(follow)
                                                .where(follow.fromMember.id.eq(memberId))
                                        )
                                )
                        )
                        .or(post.permission.eq(Permission.PRIVATE).and(post.member.id.eq(memberId)))
                ))
                .limit((long)size)
                .offset((long)(page*size))
                .fetch();
    }
}
