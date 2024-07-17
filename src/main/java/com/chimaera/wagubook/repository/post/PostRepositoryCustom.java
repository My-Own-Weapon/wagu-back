package com.chimaera.wagubook.repository.post;

import com.chimaera.wagubook.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PostRepositoryCustom {
    Page<Post> searchPostsByMemberIdAndStoreName(Long memberId, String keyword, Pageable pageable);
    List<Post> findByStoreIdAndPage(Long memberId, Long storeId, int page, int size); // 스토어 아이디와 permission기반으로 포스트 조회, 페이지네이션 추가
}
