package com.chimaera.wagubook.repository.member;

import com.chimaera.wagubook.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    boolean existsByToMemberIdAndFromMemberId(Long toMemberId, Long fromMemberId);
    Optional<Follow> findByToMemberIdAndFromMemberId(Long toMemberId, Long fromMemberId);
    List<Follow> findByFromMemberId(Long memberId);
    List<Follow> findByToMemberId(Long memberId);
    int countByFromMemberId(Long memberId);
    int countByToMemberId(Long memberId);
}
