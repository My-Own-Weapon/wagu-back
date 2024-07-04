package com.chimaera.wagubook.service;

import com.chimaera.wagubook.dto.PostRequest;
import com.chimaera.wagubook.entity.*;
import com.chimaera.wagubook.repository.category.CategoryRepository;
import com.chimaera.wagubook.repository.member.MemberRepository;
import com.chimaera.wagubook.repository.post.PostRepository;
import com.chimaera.wagubook.repository.store.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final StoreRepository storeRepository;
    private final CategoryRepository categoryRepository;

    public void createPost(PostRequest postRequest, Long userId) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 사용자 ID입니다."));

        Store store = Store.newBuilder()
                .storeName(postRequest.getStoreName())
                .storeLocation(postRequest.getStoreLocation())
                .build();
        storeRepository.save(store);

        List<Menu> menus = postRequest.getMenus().stream().map(menuRequest -> {
            Category category = Category.newBuilder()
                    .categoryName(menuRequest.getCategoryName())
                    .build();
            categoryRepository.save(category);

            return Menu.newBuilder()
                    .menuName(menuRequest.getMenuName())
                    .menuPrice(menuRequest.getMenuPrice())
                    .store(store)
                    .category(category)
                    .build();
        }).collect(Collectors.toList());

        Post post = Post.newBuilder()
                .postMainMenu(postRequest.getPostMainMenu())
                .postImage(postRequest.getPostImage())
                .postContent(postRequest.getPostContent())
                .isAuto(postRequest.isAuto())
                .member(member)
                .store(store)
                .menus(menus)
                .createDate(LocalDateTime.now())
                .permission(postRequest.getPermission())
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
        post = Post.newBuilder()
                .id(postId)
                .postMainMenu(postRequest.getPostMainMenu())
                .postImage(postRequest.getPostImage())
                .postContent(postRequest.getPostContent())
                .isAuto(postRequest.isAuto())
                .member(post.getMember())
                .store(post.getStore())
                .menus(post.getMenus())
                .createDate(post.getCreateDate())
                .permission(postRequest.getPermission())
                .build();
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
