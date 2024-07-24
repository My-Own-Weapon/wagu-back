package com.chimaera.wagubook.controller;

import com.chimaera.wagubook.dto.request.PostAIRequest;
import com.chimaera.wagubook.dto.request.PostCreateRequest;
import com.chimaera.wagubook.dto.request.PostUpdateRequest;
import com.chimaera.wagubook.dto.response.PostAIResponse;
import com.chimaera.wagubook.dto.response.PostResponse;
import com.chimaera.wagubook.dto.response.StorePostResponse;
import com.chimaera.wagubook.exception.CustomException;
import com.chimaera.wagubook.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RestController;

import com.chimaera.wagubook.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    /**
     * 포스트 생성
     * Method : POST
     * url : /posts
     * */
    @PostMapping(value = "/posts", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "포스트 생성")
    public ResponseEntity<PostResponse> createPost(@RequestPart List<MultipartFile> images, @Valid @RequestPart PostCreateRequest data, HttpSession session) throws IOException {
        Long memberId = (Long) session.getAttribute("memberId");
        checkValidByMemberId(memberId);
        return new ResponseEntity<>(postService.createPost(images, data, memberId), HttpStatus.CREATED);
    }

    @PostMapping(value = "/posts/auto")
    @Operation(summary = "AI 자동 생성")
    public ResponseEntity<PostAIResponse> createContent(@RequestBody PostAIRequest postAIRequest) throws IOException {
        return new ResponseEntity<>(postService.createContent(postAIRequest), HttpStatus.OK);
    }

    /**
     * 포스트 조회 (전체)
     * Method : GET
     * url : /posts/all
     * */
    @GetMapping("/posts/all")
    @Operation(summary = "포스트 조회 (전체)")
    public ResponseEntity<List<StorePostResponse>> getAllPosts(HttpSession session,
                                                               @RequestParam(defaultValue = "0") Integer page,
                                                               @RequestParam(defaultValue = "10") Integer size) {
        Long memberId = (Long) session.getAttribute("memberId");
        checkValidByMemberId(memberId);

        // 정렬 기준: 최신순
        Sort sort = Sort.by(Sort.Direction.DESC, "createDate");
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        return new ResponseEntity<>(postService.getAllPosts(memberId, pageRequest), HttpStatus.OK);
    }

    /**
     * 포스트 조회 (사용자)
     * Method : GET
     * url : /posts
     * */
    @GetMapping("/posts")
    @Operation(summary = "포스트 조회 (사용자)")
    public ResponseEntity<List<StorePostResponse>> getAllPostsByUser(HttpSession session,
                                                                     @RequestParam(defaultValue = "0") Integer page,
                                                                     @RequestParam(defaultValue = "10") Integer size) {
        Long memberId = (Long) session.getAttribute("memberId");
        checkValidByMemberId(memberId);

        // 정렬 기준: 최신순
        Sort sort = Sort.by(Sort.Direction.DESC, "createDate");
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        return new ResponseEntity<>(postService.getAllPostsByUser(memberId, pageRequest), HttpStatus.OK);
    }

    /**
     * 포스트 조회 (상세)
     * Method : GET
     * url : /posts/{postId}
     * */
    @GetMapping("/posts/{postId}")
    @Operation(summary = "포스트 조회 (상세)")
    public ResponseEntity<PostResponse> getPostById(@PathVariable Long postId, HttpSession session) {
        Long memberId = (Long) session.getAttribute("memberId");
        checkValidByMemberId(memberId);
        return new ResponseEntity<>(postService.getPostById(postId, memberId), HttpStatus.OK);
    }

    /**
     * 포스트 수정
     * Method : PATCH
     * url : /posts/{postId}
     * */
    @PatchMapping(value = "/posts/{postId}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "포스트 수정")
    public ResponseEntity<PostResponse> updatePost(@PathVariable Long postId, @RequestPart List<MultipartFile> images, @Valid @RequestPart PostUpdateRequest data, HttpSession session) throws IOException {
        Long memberId = (Long) session.getAttribute("memberId");
        checkValidByMemberId(memberId);
        return new ResponseEntity<>(postService.updatePost(postId, images, data, memberId), HttpStatus.OK);
    }

    /**
     * 포스트 삭제
     * Method : DELETE
     * url : /posts/{postId}
     * */
    @DeleteMapping("/posts/{postId}")
    @Operation(summary = "포스트 삭제")
    public ResponseEntity<String> deletePost(@PathVariable Long postId, HttpSession session) {
        Long memberId = (Long) session.getAttribute("memberId");
        checkValidByMemberId(memberId);
        postService.deletePost(postId, memberId);
        return new ResponseEntity<>(postId + "번 포스트가 삭제되었습니다.", HttpStatus.OK);
    }

    // 회원 검증
    private void checkValidByMemberId(Long memberId) {
        if (memberId == null) {
            throw new CustomException(ErrorCode.REQUEST_LOGIN);
        }
    }
}

