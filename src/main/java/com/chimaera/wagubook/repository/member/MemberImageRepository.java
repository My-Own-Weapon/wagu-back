package com.chimaera.wagubook.repository.member;

import com.chimaera.wagubook.entity.MemberImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberImageRepository extends JpaRepository<MemberImage, Long> {
    Optional<MemberImage> findByMemberId(Long memberId);
}
