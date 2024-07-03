package com.chimaera.wagubook.repository.member;

import com.chimaera.wagubook.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    boolean existsByFollowingIdAndFollowerId(Long followingId, Long followerId);
    Optional<Follow> findByFollowingIdAndFollowerId(Long followingId, Long followerId);
}
