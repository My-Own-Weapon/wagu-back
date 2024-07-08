package com.chimaera.wagubook.controller;

import com.chimaera.wagubook.dto.*;
import com.chimaera.wagubook.exception.CustomException;
import com.chimaera.wagubook.exception.ErrorCode;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RestController;

import com.chimaera.wagubook.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    public ResponseEntity<PostResponse> createPost(@RequestPart(required = false) List<MultipartFile> images, @RequestPart PostCreateRequest data, HttpSession session) {
        Long memberId = (Long) session.getAttribute("memberId");
        checkValidByMemberId(memberId);
        return new ResponseEntity<>(postService.createPost(images, data, memberId), HttpStatus.CREATED);
    }

    /**
     * 포스트 조회 (전체)
     * Method : GET
     * url : /posts
     * */
    @GetMapping("/posts")
    public ResponseEntity<List<StorePostResponse>> getAllPostsByUser(HttpSession session) {
        Long memberId = (Long) session.getAttribute("memberId");
        checkValidByMemberId(memberId);
        return new ResponseEntity<>(postService.getAllPostsByUser(memberId), HttpStatus.OK);
    }

    /**
     * 포스트 조회 (상세)
     * Method : GET
     * url : /posts/{postId}
     * */
    @GetMapping("/posts/{postId}")
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
    public ResponseEntity<PostResponse> updatePost(@PathVariable Long postId, @RequestPart(required = false) List<MultipartFile> files, @RequestPart PostUpdateRequest data, HttpSession session) {
        Long memberId = (Long) session.getAttribute("memberId");
        checkValidByMemberId(memberId);
        return new ResponseEntity<>(postService.updatePost(postId, files, data, memberId), HttpStatus.OK);
    }

    /**
     * 포스트 삭제
     * Method : DELETE
     * url : /posts/{postId}
     * */
    @DeleteMapping("/posts/{postId}")
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

