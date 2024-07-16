package com.chimaera.wagubook.repository.post;

import com.chimaera.wagubook.entity.Post;

import java.util.List;

public interface PostRepositoryCustom {
//    List<Post> searchPostsByMemberId(Long memberId, String keyword); // 사용자가 작성한 게시글 검색
    List<Post> searchPostsByMemberIdAndStoreName(Long memberId, String keyword); // 사용자가 작성한 게시글 중 특정 가게 검색
    public List<Post> findByStoreIdAndPage(Long memberId, Long storeId, int page, int size); // 스토어 아이디와 permission기반으로 포스트 조회, 페이지네이션 추가
}
