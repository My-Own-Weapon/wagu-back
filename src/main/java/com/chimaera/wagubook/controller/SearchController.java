package com.chimaera.wagubook.controller;

import com.chimaera.wagubook.dto.response.MemberSearchResponse;
import com.chimaera.wagubook.dto.response.PostResponse;
import com.chimaera.wagubook.dto.response.StoreSearchResponse;
import com.chimaera.wagubook.entity.Member;
import com.chimaera.wagubook.exception.CustomException;
import com.chimaera.wagubook.exception.ErrorCode;
import com.chimaera.wagubook.service.MemberService;
import com.chimaera.wagubook.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SearchController {
    private final SearchService searchService;
    private final MemberService memberService;

    /**
     * 식당 검색 (사용자)
     * Method : GET
     * url : /stores/member?keyword={keyword}
     * 사용자가 main page 에서 자신의 store 를 검색
     * */
    @GetMapping("/stores/member")
    @Operation(summary = "식당 검색 (사용자)")
    public ResponseEntity<List<PostResponse>> searchPostsByMemberIdAndStore(@RequestParam String keyword,
                                                                            @RequestParam int page,
                                                                            @RequestParam int size,
                                                                            HttpSession session) {
        Long memberId = (Long) session.getAttribute("memberId");
        checkValidByMemberId(memberId);
        Pageable pageable = PageRequest.of(page, size);
        return new ResponseEntity<>(searchService.searchPostsByMemberIdAndStoreName(memberId, keyword, pageable), HttpStatus.OK);
    }

    /**
     * 식당 검색 (전체)
     * Method : GET
     * url : /stores?keyword={keyword}
     * */
    @GetMapping("/stores")
    @Operation(summary = "식당 검색 (전체)")
    public ResponseEntity<List<StoreSearchResponse>> searchStores(@RequestParam String keyword,
                                                                  @RequestParam int page,
                                                                  @RequestParam int size,
                                                                  HttpSession session) {
        Long memberId = (Long) session.getAttribute("memberId");
        checkValidByMemberId(memberId);
        Pageable pageable = PageRequest.of(page, size);
        //TODO: store에 해당하는 전체 post의 개수도 return

        return new ResponseEntity<>(searchService.searchStores(keyword, pageable), HttpStatus.OK);
    }

    /**
     * ID로 식당 검색 (단일)
     * Method : GET
     * url : /stores/{store_id}
     * */
    @GetMapping("/store/{store_id}")
    @Operation(summary = "ID로 식당 검색 (단일)")
    public ResponseEntity<StoreSearchResponse> searchStoreById(@PathVariable String store_id,HttpSession session) {
        Long memberId = (Long) session.getAttribute("memberId");
        checkValidByMemberId(memberId);

        return new ResponseEntity<>(searchService.searchStoreById(store_id), HttpStatus.OK);
    }

    /**
     * 사용자 검색
     * Method : GET
     * url : /members?username={username}
     * */
    @GetMapping("/members")
    @Operation(summary = "사용자 검색")
    public ResponseEntity<List<MemberSearchResponse>> searchMembers(@RequestParam String username,
                                                                    @RequestParam int page,
                                                                    @RequestParam int size,
                                                                    HttpSession session) {
        Long memberId = (Long) session.getAttribute("memberId");
        Member currentMember = memberService.findById(memberId);
        checkValidByMemberId(memberId);
        Pageable pageable = PageRequest.of(page, size);
        return new ResponseEntity<>(searchService.searchMembers(username, pageable, currentMember), HttpStatus.OK);
    }


    // 회원 검증
    private void checkValidByMemberId(Long memberId) {
        if (memberId == null) {
            throw new CustomException(ErrorCode.REQUEST_LOGIN);
        }
    }


}
