package com.chimaera.wagubook.repository.post;


import com.chimaera.wagubook.entity.*;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<Post> searchPostsByMemberIdAndStoreName(Long memberId, String keyword) {
        QPost post = QPost.post;

        return queryFactory.selectFrom(post)
                .where(post.member.id.eq(memberId)
                        .and(post.store.storeName.contains(keyword)))
                .fetch();
    }

    @Override
    public List<Post> findByStoreIdAndPage(Long memberId, Long storeId, int page, int size){
        QPost post = QPost.post;
        QFollow follow = QFollow.follow;

        return queryFactory
                .selectFrom(post)
                .where(post.permission.eq(Permission.PUBLIC)
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
                )
                .limit((long)size)
                .offset((long)(page*size))
                .fetch();
    }
}
