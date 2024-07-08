package com.chimaera.wagubook.controller;

import com.chimaera.wagubook.dto.MemberSearchResponse;
import com.chimaera.wagubook.dto.PostResponse;
import com.chimaera.wagubook.dto.StoreSearchResponse;
import com.chimaera.wagubook.entity.Member;
import com.chimaera.wagubook.entity.Post;
import com.chimaera.wagubook.entity.Store;
import com.chimaera.wagubook.exception.CustomException;
import com.chimaera.wagubook.exception.ErrorCode;
import com.chimaera.wagubook.repository.member.MemberRepository;
import com.chimaera.wagubook.repository.post.PostRepository;
import com.chimaera.wagubook.repository.store.StoreRepository;
import com.chimaera.wagubook.service.SearchService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SearchController {
    private final MemberRepository memberRepository;
    private final SearchService searchService;

    // 1. Member 식당 키워드에 작성된 사용자의 포스트 중 해당되는 매장 검색
    @GetMapping("/stores/member")
    public ResponseEntity<List<PostResponse>> searchPostsByMemberAndStore(@RequestParam String keyword, HttpSession session) {
        Long memberId = (Long) session.getAttribute("memberId");
        checkValidByMemberId(memberId);
        return new ResponseEntity<>(searchService.searchPostsByMemberAndStore(memberId, keyword), HttpStatus.OK);
    }

    // 2. 식당 키워드에 작성된 전체 포스트 중 해당되는 매장 검색
    @GetMapping("/stores")
    public ResponseEntity<List<StoreSearchResponse>> searchStores(@RequestParam String keyword, HttpSession session) {
        Long memberId = (Long) session.getAttribute("memberId");
        checkValidByMemberId(memberId);
        return new ResponseEntity<>(searchService.searchStores(keyword), HttpStatus.OK);
    }

    // 3. 사용자의 아이디를 통해 전체 회원 중 해당되는 회원 검색
    @GetMapping("/members")
    public ResponseEntity<List<MemberSearchResponse>> searchMembers(@RequestParam String username, HttpSession session) {
        Long memberId = (Long) session.getAttribute("memberId");
        checkValidByMemberId(memberId);
        return new ResponseEntity<>(searchService.searchMembers(username), HttpStatus.OK);
    }

    // 회원 검증
    private void checkValidByMemberId(Long memberId) {
        if (memberId == null) {
            throw new CustomException(ErrorCode.REQUEST_LOGIN);
        }
    }
}
