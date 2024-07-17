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

import java.util.List;


@Service
@RequiredArgsConstructor
public class SearchService {

    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final StoreRepository storeRepository;

    public List<PostResponse> searchPostsByMemberIdAndStoreName(Long memberId, String keyword, Pageable pageable) {
        Page<Post> posts = postRepository.searchPostsByMemberIdAndStoreName(memberId, keyword, pageable);
        return posts.map(PostResponse::new).getContent();
    }

    public List<StoreSearchResponse> searchStores(String keyword, Pageable pageable) {
        Page<Store> stores = storeRepository.searchStores(keyword, pageable);
        return stores.map(StoreSearchResponse::new).getContent();
    }

    public List<MemberSearchResponse> searchMembers(String username, Pageable pageable, Member currentUser) {
        Page<Member> members = memberRepository.searchMembers(username, pageable);
        return members.map(member -> new MemberSearchResponse(member, currentUser)).getContent();
    }

    public StoreSearchResponse searchStoreById(String store_id) {
        Store store = storeRepository.findById(Long.parseLong(store_id)).get();
        return new StoreSearchResponse(store);
    }
}
