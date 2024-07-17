package com.chimaera.wagubook.repository.member;


import com.chimaera.wagubook.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom  {
    Optional<Member> findByUsername(String username);

}
