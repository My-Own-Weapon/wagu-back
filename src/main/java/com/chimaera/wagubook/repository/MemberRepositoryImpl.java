package com.chimaera.wagubook.repository;

import com.chimaera.wagubook.entity.Member;
import com.chimaera.wagubook.entity.QMember;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryCustom{
    private final JPAQueryFactory queryFactory;

    @Override
    public List<Member> searchMembers(String username) {
        QMember member = QMember.member;
        return queryFactory.selectFrom(member)
                .where(member.username.containsIgnoreCase(username))
                .fetch();
    }
}
