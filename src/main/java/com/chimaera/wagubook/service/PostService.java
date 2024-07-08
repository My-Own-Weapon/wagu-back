package com.chimaera.wagubook.service;

import com.chimaera.wagubook.dto.*;
import com.chimaera.wagubook.entity.*;
import com.chimaera.wagubook.exception.CustomException;
import com.chimaera.wagubook.exception.ErrorCode;
import com.chimaera.wagubook.repository.member.MemberRepository;
import com.chimaera.wagubook.repository.menu.MenuRepository;
import com.chimaera.wagubook.repository.menu.MenuImageRepository;
import com.chimaera.wagubook.repository.post.PostRepository;
import com.chimaera.wagubook.repository.store.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
    private final MenuImageRepository menuImageRepository;
    private final S3ImageService s3ImageService;

    // 포스트 생성
    public PostResponse createPost(List<MultipartFile> images, PostCreateRequest postCreateRequest, Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));

        // 전체에 있어 식당은 하나만 저장한다.
        //todo: 사용자가 이미 한번 생성한 식당은 포스트를 새로 생성할 수 없다.
        Store store = null;
        if (postCreateRequest.getStoreLocation() != null) {
            Optional<Store> findStore = storeRepository.findByStoreLocation(postCreateRequest.getStoreLocation());

            if (findStore.isPresent()) {
                store = findStore.get();
            } else {
                store = Store.newBuilder()
                        .storeName(postCreateRequest.getStoreName())
                        .storeLocation(postCreateRequest.getStoreLocation())
                        .build();
                storeRepository.save(store);
            }
        }

        // 하나의 포스트에는 여러 개의 메뉴가 달릴 수 있다.
        // 포스트당 메뉴의 이름은 중복될 수 없다.
        List<Menu> menus = new ArrayList<>();
        Set<String> menuNames = new HashSet<>();

        if (postCreateRequest.getMenus() != null) {
            for (PostCreateRequest.MenuCreateRequest menuCreateRequest : postCreateRequest.getMenus()) {
                String menuName = menuCreateRequest.getMenuName();
                if (menuName != null && !menuNames.add(menuName)) {
                    throw new CustomException(ErrorCode.DUPLICATE_POST_MENU);
                }

                Menu menu = Menu.newBuilder()
                        .menuName(menuName)
                        .menuPrice(menuCreateRequest.getMenuPrice())
                        .menuContent(menuCreateRequest.getMenuContent())
                        .store(store)
                        .build();
                menus.add(menu);
            }
        }

        Post post = Post.newBuilder()
                .member(member)
                .store(store)
                .menus(menus)
                .postMainMenu(postCreateRequest.getPostMainMenu())
                .category(postCreateRequest.getPostCategory())
                .permission(postCreateRequest.getPermission())
                .isAuto(postCreateRequest.isAuto())
                .createDate(LocalDateTime.now())
                .build();
        postRepository.save(post);

        for (int i = 0; i < menus.size(); i++) {
            Menu menu = menus.get(i);
            menu.setPost(post);
            menuRepository.save(menu);

            if (images != null) {
                if (menus.size() < images.size()) {
                    throw new CustomException(ErrorCode.IMAGE_SIZE_IS_FULL);
                }

                MultipartFile image = images.get(i);
                String url = s3ImageService.upload(image);

                MenuImage menuImage = MenuImage.newBuilder()
                        .url(url)
                        .menu(menu)
                        .build();
                menuImageRepository.save(menuImage);

                menu.setMenuImage(menuImage);
                menuRepository.save(menu);
            }
        }

        // Request 값이 하나라도 null인 경우, isFinished 값을 false로 처리한다. (임시 포스트 생성)
        if (postCreateRequest.getStoreName() == null || postCreateRequest.getStoreLocation() == null ||
                postCreateRequest.getPostCategory() == null || postCreateRequest.getPostMainMenu() == null ||
                postCreateRequest.getPermission() == null || postCreateRequest.getMenus() == null) {

            post.updateFinished(false);
        } else {
            post.updateFinished(true);
        }

        for (PostCreateRequest.MenuCreateRequest menuCreateRequest : postCreateRequest.getMenus()) {
            if (menuCreateRequest.getMenuContent() == null || menuCreateRequest.getMenuName() == null || menuCreateRequest.getMenuPrice() == 0) {
                post.updateFinished(false);
            } else {
                post.updateFinished(true);
            }
        }

        return new PostResponse(post);
    }

    // 포스트 조회 (전체)
    public List<StorePostResponse> getAllPostsByUser(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));
        List<Post> posts = postRepository.findAllByMemberId(member.getId());

        return posts.stream()
                .map(post -> {
                    // 보내지는 정보는 사용자가 작성한 Main Menu 기준으로
                    // 일치하는 것이 없을 경우, 첫번째 menu를 보내주기
                    // 사용자의 생성 포스트를 확인함으로 임시 포스트일 경우도 고려한다.
                    List<Menu> menus = post.getMenus();
                    if (menus == null) {
                        return new StorePostResponse(post, null);
                    }

                    String mainMenu = post.getPostMainMenu();
                    if (mainMenu == null) {
                        return new StorePostResponse(post, menus.get(0));
                    }

                    Optional<Menu> findMenu = menuRepository.findByMenuName(mainMenu);
                    if (findMenu.isPresent()) {
                        Menu menu = findMenu.get();
                        return new StorePostResponse(post, menu);
                    } else {
                        return new StorePostResponse(post, menus.get(0));
                    }
                })
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
    public PostResponse updatePost(Long postId, List<MultipartFile> images, PostUpdateRequest postUpdateRequest, Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));
        Post post = postRepository.findByIdAndMemberId(postId, member.getId()).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_POST));

        // 전체에 있어 식당은 하나만 저장한다.
        //todo: 사용자가 이미 한번 생성한 식당은 포스트를 새로 생성할 수 없다.
        Store store = null;
        if (postUpdateRequest.getStoreLocation() != null) {
            Optional<Store> findStore = storeRepository.findByStoreLocation(postUpdateRequest.getStoreLocation());

            if (findStore.isPresent()) {
                store = findStore.get();
            } else if (postUpdateRequest.getStoreName() != null) {
                store = Store.newBuilder()
                        .storeName(postUpdateRequest.getStoreName())
                        .storeLocation(postUpdateRequest.getStoreLocation())
                        .build();
                storeRepository.save(store);
            }
        }

        // 하나의 포스트에는 여러 개의 메뉴가 달릴 수 있다.
        // 포스트당 메뉴의 이름은 중복될 수 없다.
        // 기존에 작성한 메뉴를 수정하거나, 새로운 메뉴를 추가할 수 있다. (menuId가 null이 아닌 경우, 기존에 작성한 메뉴를 수정한 것이고, 아닐 경우, 새로운 메뉴를 추가한다.)
        List<Menu> menus = new ArrayList<>();
        Set<String> menuNames = new HashSet<>();
        for (PostUpdateRequest.MenuUpdateRequest menuUpdateRequest : postUpdateRequest.getMenus()) {
            Optional<Menu> findMenu = menuRepository.findByIdAndPost(menuUpdateRequest.getMenuId(), post);

            if (findMenu.isPresent()) {
                Menu menu = findMenu.get();
                menu.updateMenu(menuUpdateRequest.getMenuName(), menuUpdateRequest.getMenuPrice(), menuUpdateRequest.getMenuContent());
                menus.add(menu);
            } else if (!menuNames.add(menuUpdateRequest.getMenuName())) {
                throw new CustomException(ErrorCode.DUPLICATE_POST_MENU);
            } else {
                Menu menu = Menu.newBuilder()
                        .menuName(menuUpdateRequest.getMenuName())
                        .menuPrice(menuUpdateRequest.getMenuPrice())
                        .menuContent(menuUpdateRequest.getMenuContent())
                        .store(store)
                        .build();
                menus.add(menu);
                menuRepository.save(menu);
            }
        }

        post.updatePost(store, menus, postUpdateRequest.getPostMainMenu(), postUpdateRequest.getPostCategory(), postUpdateRequest.getPermission(), postUpdateRequest.isAuto());
        postRepository.save(post);

        for (int i = 0; i < menus.size(); i++) {
            Menu menu = menus.get(i);
            menu.setPost(post);
            menuRepository.save(menu);

            if (images != null) {
                if (menus.size() < images.size()) {
                    throw new CustomException(ErrorCode.IMAGE_SIZE_IS_FULL);
                }

                // 새로운 이미지 업로드를 위해 기존 이미지 삭제
                //todo: 사용자가 어떤 사진을 수정하고 싶은지 식별할 수 있는 방법이 있을지 고민해보기
                MenuImage oldMenuImage = menu.getMenuImage();

                if (oldMenuImage != null) {
                    s3ImageService.deleteImageFromS3(oldMenuImage.getUrl());
                    menuImageRepository.delete(oldMenuImage);
                }

                MultipartFile image = images.get(i);
                String url = s3ImageService.upload(image);

                MenuImage menuImage = MenuImage.newBuilder()
                        .url(url)
                        .menu(menu)
                        .build();
                menuImageRepository.save(menuImage);

                menu.setMenuImage(menuImage);
                menuRepository.save(menu);
            }
        }

        // Request 값이 하나라도 null인 경우, isFinished 값을 false로 처리한다. (임시 포스트 생성)
        if (postUpdateRequest.getStoreName() == null || postUpdateRequest.getStoreLocation() == null ||
                postUpdateRequest.getPostCategory() == null || postUpdateRequest.getPostMainMenu() == null ||
                postUpdateRequest.getPermission() == null || postUpdateRequest.getMenus() == null) {

            post.updateFinished(false);
        } else {
            post.updateFinished(true);
        }

        for (PostUpdateRequest.MenuUpdateRequest menuUpdateRequest : postUpdateRequest.getMenus()) {
            if (menuUpdateRequest.getMenuContent() == null || menuUpdateRequest.getMenuName() == null || menuUpdateRequest.getMenuPrice() == 0) {
                post.updateFinished(false);
            } else {
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
