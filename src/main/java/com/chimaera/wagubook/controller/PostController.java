package com.chimaera.wagubook.controller;

import com.chimaera.wagubook.dto.PostResponse;
import com.chimaera.wagubook.exception.CustomException;
import com.chimaera.wagubook.exception.ErrorCode;
import com.chimaera.wagubook.service.S3ImageService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.RestController;

import com.chimaera.wagubook.dto.PostRequest;
import com.chimaera.wagubook.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    @PostMapping("/posts")
    public ResponseEntity<PostResponse> createPost(@RequestBody PostRequest postRequest, HttpSession session) {
        Long memberId = (Long) session.getAttribute("memberId");
        checkValidByMemberId(memberId);
        return new ResponseEntity<>(postService.createPost(postRequest, memberId), HttpStatus.CREATED);
    }

    /**
     * 포스트 조회 (전체)
     * Method : GET
     * url : /posts
     * */
    @GetMapping("/posts")
    public ResponseEntity<List<PostResponse>> getAllPostsByUser(HttpSession session) {
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
    @PatchMapping("/posts/{postId}")
    public ResponseEntity<PostResponse> updatePost(@PathVariable Long postId, @RequestBody PostRequest postRequest, HttpSession session) {
        Long memberId = (Long) session.getAttribute("memberId");
        checkValidByMemberId(memberId);
        return new ResponseEntity<>(postService.updatePost(postId, postRequest, memberId), HttpStatus.OK);
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

