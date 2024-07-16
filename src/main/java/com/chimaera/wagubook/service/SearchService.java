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
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final StoreRepository storeRepository;

    public List<PostResponse> searchPostsByMemberIdAndStoreName(Long memberId, String keyword) {
        List<Post> posts = postRepository.searchPostsByMemberIdAndStoreName(memberId, keyword);
        return posts.stream()
                .map(post -> new PostResponse(post))
                .collect(Collectors.toList());
    }

    public List<StoreSearchResponse> searchStores(String keyword) {
        List<Store> stores = storeRepository.searchStores(keyword);
        return stores.stream()
                .map(store -> new StoreSearchResponse(store))
                .collect(Collectors.toList());
    }

    public List<MemberSearchResponse> searchMembers(String username) {
        List<Member> members = memberRepository.searchMembers(username);
        return members.stream()
                .map(member -> new MemberSearchResponse(member))
                .collect(Collectors.toList());
    }
}
