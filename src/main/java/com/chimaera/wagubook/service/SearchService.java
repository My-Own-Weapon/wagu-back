package com.chimaera.wagubook.service;


import com.chimaera.wagubook.dto.response.MemberSearchResponse;
import com.chimaera.wagubook.dto.response.PostResponse;
import com.chimaera.wagubook.dto.response.StoreSearchResponse;
import com.chimaera.wagubook.entity.Member;
import com.chimaera.wagubook.entity.Post;
import com.chimaera.wagubook.entity.Store;
import com.chimaera.wagubook.repository.member.MemberRepository;
import com.chimaera.wagubook.repository.post.PostRepository;
import com.chimaera.wagubook.repository.store.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class SearchService {

    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final StoreRepository storeRepository;

    public Page<PostResponse> searchPostsByMemberIdAndStoreName(Long memberId, String keyword, Pageable pageable) {
        Page<Post> posts = postRepository.searchPostsByMemberIdAndStoreName(memberId, keyword, pageable);
        return posts.map(PostResponse::new);
    }

    public Page<StoreSearchResponse> searchStores(String keyword, Pageable pageable) {
        Page<Store> stores = storeRepository.searchStores(keyword, pageable);
        return stores.map(StoreSearchResponse::new);
    }

    public Page<MemberSearchResponse> searchMembers(String username, Pageable pageable) {
        Page<Member> members = memberRepository.searchMembers(username, pageable);
        return members.map(MemberSearchResponse::new);
    }

}
