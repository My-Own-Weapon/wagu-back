package com.chimaera.wagubook.repository.post;

import com.chimaera.wagubook.entity.Post;

import java.util.List;

public interface PostRepositoryCustom {
    List<Post> searchPostsByMember(Long memberId, String keyword); // 사용자가 작성한 게시글 검색
}
