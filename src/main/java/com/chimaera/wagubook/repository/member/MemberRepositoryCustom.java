package com.chimaera.wagubook.repository.member;

import com.chimaera.wagubook.entity.Member;

import java.util.List;

public interface MemberRepositoryCustom {
    List<Member> searchMembers(String username); // 사용자 검색
}
