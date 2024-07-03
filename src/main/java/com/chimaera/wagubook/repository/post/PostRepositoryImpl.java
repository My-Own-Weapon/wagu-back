package com.chimaera.wagubook.repository.post;

import com.chimaera.wagubook.entity.Post;
import com.chimaera.wagubook.entity.QPost;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom{
    private final JPAQueryFactory queryFactory;

    @Override
    public List<Post> searchPostsByMember(Long memberId, String keyword) {
        QPost post = QPost.post;

        return queryFactory
                .selectFrom(post)
                .where(post.member.id.eq(memberId)
                        .and(post.postMainMenu.containsIgnoreCase(keyword)))
                .fetch();
    }
}
