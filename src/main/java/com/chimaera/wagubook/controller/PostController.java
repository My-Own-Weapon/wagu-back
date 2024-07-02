package com.chimaera.wagubook.controller;

import com.chimaera.wagubook.exception.CustomException;
import com.chimaera.wagubook.exception.ErrorCode;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.RestController;

import com.chimaera.wagubook.dto.PostRequest;
import com.chimaera.wagubook.entity.Post;
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

    @PostMapping("/posts")
    public ResponseEntity<String> createPost(@RequestBody PostRequest postRequest, HttpSession session) {
        Long memberId = (Long) session.getAttribute("memberId");
        if (memberId == null) {
            throw new CustomException(ErrorCode.REQUEST_LOGIN);
        }
        postService.createPost(postRequest, memberId);
        return new ResponseEntity<>("포스팅이 생성되었습니다.", HttpStatus.CREATED);
    }

    @GetMapping("/posts")
    public ResponseEntity<List<Post>> getAllPosts(HttpSession session) {
        Long memberId = (Long) session.getAttribute("memberId");
        if (memberId == null) {
            throw new CustomException(ErrorCode.REQUEST_LOGIN);
        }
        List<Post> posts = postService.getAllPostsByUser(memberId);
        return new ResponseEntity<>(posts, HttpStatus.OK);
    }

    @GetMapping("/posts/{postId}")
    public ResponseEntity<Post> getPostById(@PathVariable Long postId, HttpSession session) {
        Long memberId = (Long) session.getAttribute("memberId");
        if (memberId == null) {
            throw new CustomException(ErrorCode.REQUEST_LOGIN);
        }
        Post post = postService.getPostById(postId, memberId);
        if (post == null) {
            throw new CustomException(ErrorCode.NOT_FOUND_POST);
        }
        return new ResponseEntity<>(post, HttpStatus.OK);
    }

    @PatchMapping("/posts/{postId}")
    public ResponseEntity<String> updatePost(@PathVariable Long postId, @RequestBody PostRequest postRequest, HttpSession session) {
        Long memberId = (Long) session.getAttribute("memberId");
        if (memberId == null) {
            throw new CustomException(ErrorCode.REQUEST_LOGIN);
        }
        boolean updated = postService.updatePost(postId, postRequest, memberId);
        if (!updated) {
            throw new CustomException(ErrorCode.UNABLE_TO_UPDATE_POST);
        }
        return new ResponseEntity<>("포스트가 수정되었습니다.", HttpStatus.OK);
    }

    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<String> deletePost(@PathVariable Long postId, HttpSession session) {
        Long memberId = (Long) session.getAttribute("memberId");
        if (memberId == null) {
            throw new CustomException(ErrorCode.REQUEST_LOGIN);
        }
        boolean deleted = postService.deletePost(postId, memberId);
        if (!deleted) {
            throw new CustomException(ErrorCode.UNABLE_TO_DELETE_POST);
        }
        return new ResponseEntity<>("포스트가 삭제되었습니다.", HttpStatus.OK);
    }
}

