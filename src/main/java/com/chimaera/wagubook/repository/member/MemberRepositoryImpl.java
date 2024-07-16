package com.chimaera.wagubook.repository.member;

import com.chimaera.wagubook.entity.Member;
import com.chimaera.wagubook.entity.QMember;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryCustom{
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Member> searchMembers(String username, Pageable pageable) {
        QMember member = QMember.member;

        List<Member> members = queryFactory.selectFrom(member)
                .where(member.username.containsIgnoreCase(username))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory.selectFrom(member)
                .where(member.username.containsIgnoreCase(username))
                .fetchCount();

        return new PageImpl<>(members, pageable, total);
    }
}
