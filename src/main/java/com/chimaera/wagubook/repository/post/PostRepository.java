package com.chimaera.wagubook.repository.post;

import com.chimaera.wagubook.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom{
    Optional<Post> findByIdAndMemberId(Long postId, Long memberId);
    Page<Post> findAllByMemberId(Long memberId, PageRequest pageRequest);
    List<Post> findAllByStoreId(Long storeId);
    int countByMemberId(Long memberId);
    List<Post> searchPostsByMemberIdAndStoreName(Long memberId, String keyword);
    boolean existsByStoreIdAndMemberId(Long storeId, Long memberId);
}
