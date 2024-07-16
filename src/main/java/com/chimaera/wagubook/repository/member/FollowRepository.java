package com.chimaera.wagubook.repository.member;

import com.chimaera.wagubook.entity.Follow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    boolean existsByToMemberIdAndFromMemberId(Long toMemberId, Long fromMemberId);
    Optional<Follow> findByToMemberIdAndFromMemberId(Long toMemberId, Long fromMemberId);
    Page<Follow> findByFromMemberId(Long memberId, PageRequest pageRequest);
    Page<Follow> findByToMemberId(Long memberId, PageRequest pageRequest);
    int countByFromMemberId(Long memberId);
    int countByToMemberId(Long memberId);
}
