package com.chimaera.wagubook.repository.member;

import com.chimaera.wagubook.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MemberRepositoryCustom {
    Page<Member> searchMembers(String username, Pageable pageable); // 사용자 검색
}
