package com.chimaera.wagubook.controller;

import com.chimaera.wagubook.entity.Member;
import com.chimaera.wagubook.entity.Post;
import com.chimaera.wagubook.entity.Store;
import com.chimaera.wagubook.repository.member.MemberRepository;
import com.chimaera.wagubook.repository.post.PostRepository;
import com.chimaera.wagubook.repository.store.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SearchController {
    private final StoreRepository storeRepository;
    private final MemberRepository memberRepository;
    private final PostRepository postRepository;

    // 1. Member 식당 키워드에 작성된 사용자의 포스트 중 해당되는 매장 검색
    @GetMapping("/stores/member")
    public List<Post> searchPostsByMemberAndStore(
            @RequestParam Long memberId,
            @RequestParam String keyword) {
        return postRepository.searchPostsByMember(memberId, keyword);
    }

    // 2. 식당 키워드에 작성된 전체 포스트 중 해당되는 매장 검색
    @GetMapping("/stores")
    public List<Store> searchStores(@RequestParam String keyword) {
        return storeRepository.searchStores(keyword);
    }

    // 3. 사용자의 아이디를 통해 전체 회원 중 해당되는 회원 검색
    @GetMapping("/members")
    public List<Member> searchMembers(@RequestParam String username) {
        return memberRepository.searchMembers(username);
    }
}
