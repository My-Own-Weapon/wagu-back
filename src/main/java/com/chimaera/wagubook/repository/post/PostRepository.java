package com.chimaera.wagubook.repository.post;

import com.chimaera.wagubook.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom{
    Optional<Post> findByIdAndMemberId(Long postId, Long memberId);
    List<Post> findAllByMemberId(Long memberId);
    List<Post> findAllByStoreId(Long storeId);
    int countByMemberId(Long memberId);
}
