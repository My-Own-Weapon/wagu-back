package com.chimaera.wagubook.service;

import com.chimaera.wagubook.dto.PostRequest;
import com.chimaera.wagubook.dto.PostResponse;
import com.chimaera.wagubook.entity.*;
import com.chimaera.wagubook.exception.CustomException;
import com.chimaera.wagubook.exception.ErrorCode;
import com.chimaera.wagubook.repository.member.MemberRepository;
import com.chimaera.wagubook.repository.menu.MenuRepository;
import com.chimaera.wagubook.repository.post.PostRepository;
import com.chimaera.wagubook.repository.store.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final StoreRepository storeRepository;
    private final MenuRepository menuRepository;

    // 포스트 생성
    public PostResponse createPost(PostRequest postRequest, Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));

        // 전체에 있어 식당은 하나만 저장한다.
        //todo: 사용자가 이미 한번 생성한 식당은 포스트를 새로 생성할 수 없다.
        Store store = null;
        if (postRequest.getStoreLocation() != null) {
            Optional<Store> findStore = storeRepository.findByStoreLocation(postRequest.getStoreLocation());

            if (findStore.isPresent()) {
                store = findStore.get();
            } else {
                store = Store.newBuilder()
                        .storeName(postRequest.getStoreName())
                        .storeLocation(postRequest.getStoreLocation())
                        .build();
                storeRepository.save(store);
            }
        }

        // 하나의 포스트에는 여러 개의 메뉴가 달릴 수 있다.
        // 포스트당 메뉴의 이름은 중복될 수 없다.
        List<Menu> menus = new ArrayList<>();
        Set<String> menuNames = new HashSet<>();

        if (postRequest.getMenus() != null) {
            for (PostRequest.MenuRequest menuRequest : postRequest.getMenus()) {
                String menuName = menuRequest.getMenuName();
                if (menuName != null && !menuNames.add(menuName)) {
                    throw new CustomException(ErrorCode.DUPLICATE_POST_MENU);
                }

                Menu menu = Menu.newBuilder()
                        .menuName(menuName)
                        .menuPrice(menuRequest.getMenuPrice())
                        .menuImage(null) // todo: 이미지 저장
                        .menuContent(menuRequest.getMenuContent())
                        .store(store)
                        .build();
                menus.add(menu);
            }
        }

        Post post = Post.newBuilder()
                .member(member)
                .store(store)
                .menus(menus)
                .postMainMenu(postRequest.getPostMainMenu())
                .category(postRequest.getPostCategory())
                .permission(postRequest.getPermission())
                .isAuto(postRequest.isAuto())
                .isFinished(true)
                .createDate(LocalDateTime.now())
                .build();
        postRepository.save(post);

        for (Menu menu : menus) {
            menu.setPost(post);
            menuRepository.save(menu);
        }

        // Request 값이 하나라도 null인 경우, isFinished 값을 false로 처리한다. (임시 포스트 생성)
        if (postRequest.getStoreName() == null || postRequest.getStoreLocation() == null ||
                postRequest.getPostCategory() == null || postRequest.getPostMainMenu() == null ||
                postRequest.getPermission() == null || postRequest.getMenus() == null) {

            post.updateFinished(false);
        }

        for (PostRequest.MenuRequest menuRequest : postRequest.getMenus()) {
            if (menuRequest.getMenuContent() == null || menuRequest.getMenuName() == null || menuRequest.getMenuPrice() == 0) {
                post.updateFinished(false);
            }
        }

        return new PostResponse(post);
    }

    // 포스트 조회 (전체)
    public List<PostResponse> getAllPostsByUser(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));
        List<Post> posts = postRepository.findAllByMemberId(member.getId());

        return posts.stream()
                .map(PostResponse::new)
                .collect(Collectors.toList());
    }

    // 포스트 조회 (상세)
    public PostResponse getPostById(Long postId, Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));
        Post post = postRepository.findByIdAndMemberId(postId, member.getId()).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_POST));

        if (!post.getMember().getId().equals(member.getId())) {
            throw new CustomException(ErrorCode.FORBIDDEN_MEMBER);
        }

        return new PostResponse(post);
    }

    // 포스트 수정
    public PostResponse updatePost(Long postId, PostRequest postRequest, Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));
        Post post = postRepository.findByIdAndMemberId(postId, member.getId()).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_POST));

        // 전체에 있어 식당은 하나만 저장한다.
        //todo: 사용자가 이미 한번 생성한 식당은 포스트를 새로 생성할 수 없다.
        Store store = null;
        if (postRequest.getStoreLocation() != null) {
            Optional<Store> findStore = storeRepository.findByStoreLocation(postRequest.getStoreLocation());

            if (findStore.isPresent()) {
                store = findStore.get();
            } else if (postRequest.getStoreName() != null) {
                store = Store.newBuilder()
                        .storeName(postRequest.getStoreName())
                        .storeLocation(postRequest.getStoreLocation())
                        .build();
                storeRepository.save(store);
            }
        }

        // 하나의 포스트에는 여러 개의 메뉴가 달릴 수 있다.
        // 포스트당 메뉴의 이름은 중복될 수 없다.
        // 기존에 작성한 메뉴를 수정하거나, 새로운 메뉴를 추가할 수 있다.
        List<Menu> menus = new ArrayList<>();
        Set<String> menuNames = new HashSet<>();
        for (PostRequest.MenuRequest menuRequest : postRequest.getMenus()) {
            Optional<Menu> findMenu = menuRepository.findByMenuNameAndPost(menuRequest.getMenuName(), post);

            if (findMenu.isPresent()) {
                Menu menu = findMenu.get();
                menu.updateMenu(menuRequest.getMenuName(), menuRequest.getMenuPrice(), menuRequest.getMenuContent());
                menus.add(menu);
            } else if (!menuNames.add(menuRequest.getMenuName())) {
                throw new CustomException(ErrorCode.DUPLICATE_POST_MENU);
            } else {
                Menu menu = Menu.newBuilder()
                        .menuName(menuRequest.getMenuName())
                        .menuPrice(menuRequest.getMenuPrice())
                        .menuImage(null) //todo: 이미지 저장
                        .menuContent(menuRequest.getMenuContent())
                        .store(store)
                        .build();
                menus.add(menu);
                menuRepository.save(menu);
            }
        }

        post.updatePost(store, menus, postRequest.getPostMainMenu(), postRequest.getPostCategory(), postRequest.getPermission(), postRequest.isAuto());
        postRepository.save(post);

        // 수정 시 null로 채워진 것이 하나도 없을 경우, isFinished 값을 true로 바꿔준다.
        if (postRequest.getStoreName() == null && postRequest.getStoreLocation() == null &&
                postRequest.getPostCategory() == null && postRequest.getPostMainMenu() == null &&
                postRequest.getPermission() == null && postRequest.getMenus() == null) {

            post.updateFinished(true);
        }

        for (PostRequest.MenuRequest menuRequest : postRequest.getMenus()) {
            if (menuRequest.getMenuContent() == null && menuRequest.getMenuName() == null && menuRequest.getMenuPrice() == 0) {
                post.updateFinished(true);
            }
        }

        return new PostResponse(post);
    }

    // 포스트 삭제
    public void deletePost(Long postId, Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));
        Post post = postRepository.findByIdAndMemberId(postId, member.getId()).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_POST));
        postRepository.delete(post);
    }
}
