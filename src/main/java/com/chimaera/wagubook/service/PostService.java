package com.chimaera.wagubook.service;

import com.chimaera.wagubook.dto.PostRequest;
import com.chimaera.wagubook.entity.Member;
import com.chimaera.wagubook.entity.Post;
import com.chimaera.wagubook.repository.MemberRepository;
import com.chimaera.wagubook.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    public void createPost(PostRequest postRequest, Long userId) {
        Member member = memberRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("유효하지 않은 사용자 ID입니다."));
        Post post = Post.builder()
                .postMainMenu(postRequest.getPostMainMenu())
                .postImage(postRequest.getPostImage())
                .postContent(postRequest.getPostContent())
                .isAuto(postRequest.isAuto())
                .member(member)
                .createDate(LocalDateTime.now())
                .build();
        postRepository.save(post);
    }

    public List<Post> getAllPostsByUser(Long userId) {
        Member member = memberRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("유효하지 않은 사용자 ID입니다."));
        return member.getPosts();
    }

    public Post getPostById(Long postId, Long userId) {
        return postRepository.findById(postId)
                .filter(post -> post.getMember().getId().equals(userId))
                .orElse(null);
    }

    public boolean updatePost(Long postId, PostRequest postRequest, Long userId) {
        Post post = getPostById(postId, userId);
        if (post == null) {
            return false;
        }
        post.setPostMainMenu(postRequest.getPostMainMenu());
        post.setPostImage(postRequest.getPostImage());
        post.setPostContent(postRequest.getPostContent());
        post.setAuto(postRequest.isAuto());
        post.setUpdateDate(LocalDateTime.now());
        postRepository.save(post);
        return true;
    }

    public boolean deletePost(Long postId, Long userId) {
        Post post = getPostById(postId, userId);
        if (post == null) {
            return false;
        }
        postRepository.delete(post);
        return true;
    }
}
