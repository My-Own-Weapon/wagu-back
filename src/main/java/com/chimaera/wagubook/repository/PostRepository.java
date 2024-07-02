package com.chimaera.wagubook.repository;

import com.chimaera.wagubook.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    Optional<Post> findByIdAndMemberId(Long id, Long memberId);
    List<Post> findAllByMemberId(Long memberId);
}
