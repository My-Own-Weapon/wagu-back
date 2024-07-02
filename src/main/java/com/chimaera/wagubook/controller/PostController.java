package com.chimaera.wagubook.controller;

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
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return new ResponseEntity<>("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED);
        }
        postService.createPost(postRequest, userId);
        return new ResponseEntity<>("포스팅이 생성되었습니다.", HttpStatus.CREATED);
    }

    @GetMapping("/posts")
    public ResponseEntity<List<Post>> getAllPosts(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        List<Post> posts = postService.getAllPostsByUser(userId);
        return new ResponseEntity<>(posts, HttpStatus.OK);
    }

    @GetMapping("/posts/{postId}")
    public ResponseEntity<Post> getPostById(@PathVariable Long postId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Post post = postService.getPostById(postId, userId);
        if (post == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(post, HttpStatus.OK);
    }

    @PatchMapping("/posts/{postId}")
    public ResponseEntity<String> updatePost(@PathVariable Long postId, @RequestBody PostRequest postRequest, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return new ResponseEntity<>("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED);
        }
        boolean updated = postService.updatePost(postId, postRequest, userId);
        if (!updated) {
            return new ResponseEntity<>("포스트를 수정할 수 없습니다.", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>("포스트가 수정되었습니다.", HttpStatus.OK);
    }

    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<String> deletePost(@PathVariable Long postId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return new ResponseEntity<>("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED);
        }
        boolean deleted = postService.deletePost(postId, userId);
        if (!deleted) {
            return new ResponseEntity<>("포스트를 삭제할 수 없습니다.", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>("포스트가 삭제되었습니다.", HttpStatus.OK);
    }
}

