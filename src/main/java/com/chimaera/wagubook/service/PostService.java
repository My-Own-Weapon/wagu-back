package com.chimaera.wagubook.service;

import com.chimaera.wagubook.dto.request.PostAIRequest;
import com.chimaera.wagubook.dto.request.PostCreateRequest;
import com.chimaera.wagubook.dto.request.PostUpdateRequest;
import com.chimaera.wagubook.dto.response.PostAIResponse;
import com.chimaera.wagubook.dto.response.PostResponse;
import com.chimaera.wagubook.dto.response.PostUpdateCheckResponse;
import com.chimaera.wagubook.dto.response.StorePostResponse;
import com.chimaera.wagubook.entity.*;
import com.chimaera.wagubook.exception.CustomException;
import com.chimaera.wagubook.exception.ErrorCode;
import com.chimaera.wagubook.repository.member.FollowRepository;
import com.chimaera.wagubook.repository.member.MemberRepository;
import com.chimaera.wagubook.repository.menu.MenuRepository;
import com.chimaera.wagubook.repository.menu.MenuImageRepository;
import com.chimaera.wagubook.repository.post.PostRepository;
import com.chimaera.wagubook.repository.store.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.IOException;
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
    private final FollowRepository followRepository;
    private final MenuImageRepository menuImageRepository;
    private final S3ImageService s3ImageService;
    private final OpenAiService openAiService;

    // 포스트 생성
    @Transactional
    public PostResponse createPost(List<MultipartFile> images, PostCreateRequest postCreateRequest, Long memberId) throws IOException {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));

        // 전체에 있어 식당은 하나만 저장한다.
        // 사용자가 이미 한번 생성한 식당은 포스트를 새로 생성할 수 없다.
        Store store = null;
        if (postCreateRequest.getStoreLocation() != null) {
            Optional<Store> findStore = storeRepository.findByStoreLocation(postCreateRequest.getStoreLocation());

            if (findStore.isPresent()) {
                store = findStore.get();

                if (postRepository.existsByStoreIdAndMemberId(store.getId(), memberId)) {
                    throw new CustomException(ErrorCode.DUPLICATE_POST_STORE);
                }

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
                menuRepository.save(menu);
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

        // 이미지 개수 = 메뉴 개수일 경우, 이미지 개수는 순서대로 Menu와 1:1 대응시켜 저장해준다.
        // 이미지 개수 != 메뉴 개수일 경우, 예외 처리 해준다.
        if (images == null || images.size() != menus.size()) {
            throw new CustomException(ErrorCode.IMAGE_NOT_EQUAL_WITH_MENU);
        }

        for (int i = 0; i < menus.size(); i++) {
            Menu menu = menus.get(i);

            MultipartFile image = images.get(i);
            BufferedImage resizedImage = s3ImageService.resizeImageWithAspectRatio(image, 512, 512);

//            if (postCreateRequest.isAuto()) {
////                String review = openAiService.requestImageAnalysis(resizedImage, menu.getMenuName(), postCreateRequest.getPostCategory().toString());
//                String review = openAiService.requestText(menu.getMenuName(), postCreateRequest.getPostCategory().toString());
//
//                menu.setMenuContent(review);
//                menuRepository.save(menu);
//            }

            String url = s3ImageService.uploadImage(resizedImage, image.getOriginalFilename());

            MenuImage menuImage = MenuImage.newBuilder()
                    .url(url)
                    .menu(menu)
                    .build();
            menuImageRepository.save(menuImage);

            menu.setPost(post);
            menu.setMenuImage(menuImage);
            menuRepository.save(menu);
        }

        return new PostResponse(post);
    }

    public PostAIResponse createContent(PostAIRequest postAIRequest) throws IOException {
        String review = openAiService.requestText(postAIRequest.getMenuName(), postAIRequest.getPostCategory().toString());
        return new PostAIResponse(review);
    }

    // 포스트 조회 (전체)
    public List<StorePostResponse> getAllPosts(Long memberId, PageRequest pageRequest) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));
        List<Follow> followingsList = followRepository.findByToMemberId(memberId);
        List<Post> allPosts = postRepository.findAll();

        // 포스트 권한에 따른 필터링
        List<Post> filteredPosts = allPosts.stream()
                .filter(post -> {
                    if (post.getPermission() == Permission.PUBLIC) {
                        return true;
                    } else if (post.getPermission() == Permission.PRIVATE) {
                        return post.getMember().getId().equals(member.getId());
                    } else if (post.getPermission() == Permission.FRIENDS) {
                        boolean isFollower = followingsList.stream()
                                .anyMatch(follow -> follow.getFromMember().getId().equals(post.getMember().getId()));
                        return isFollower || post.getMember().getId().equals(member.getId());
                    }
                    return false;
                })
                .collect(Collectors.toList());

        // 페이징 적용
        int start = (int) pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), filteredPosts.size());
        List<Post> pagedPosts = filteredPosts.subList(start, end);

        return pagedPosts.stream()
                .map(post -> {
                    // 보내지는 정보는 사용자가 작성한 Main Menu 기준으로
                    // 일치하는 것이 없을 경우, 첫번째 menu를 보내주기
                    // 사용자의 생성 포스트를 확인함으로 임시 포스트일 경우도 고려한다.
                    List<Menu> menus = post.getMenus();
                    if (menus == null || menus.isEmpty()) {
                        return new StorePostResponse(post, null);
                    }

                    String mainMenu = post.getPostMainMenu();
                    if (mainMenu == null || menus.isEmpty()) {
                        return new StorePostResponse(post, menus.get(0));
                    }

                    Optional<Menu> findMenu = menuRepository.findByMenuNameAndPostId(mainMenu, post.getId());
                    if (findMenu.isPresent()) {
                        Menu menu = findMenu.get();
                        return new StorePostResponse(post, menu);
                    } else {
                        return new StorePostResponse(post, menus.get(0));
                    }
                })
                .collect(Collectors.toList());
    }

    // 포스트 조회 (사용자)
    public List<StorePostResponse> getAllPostsByUser(Long memberId, PageRequest pageRequest) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));
        Page<Post> postsPage = postRepository.findAllByMemberId(member.getId(), pageRequest);

        return postsPage.getContent().stream()
                .map(post -> {
                    // 보내지는 정보는 사용자가 작성한 Main Menu 기준으로
                    // 일치하는 것이 없을 경우, 첫번째 menu를 보내주기
                    // 사용자의 생성 포스트를 확인함으로 임시 포스트일 경우도 고려한다.
                    List<Menu> menus = post.getMenus();
                    if (menus == null || menus.isEmpty()) {
                        return new StorePostResponse(post, null);
                    }

                    String mainMenu = post.getPostMainMenu();
                    if (mainMenu == null || menus.isEmpty()) {
                        return new StorePostResponse(post, menus.get(0));
                    }

                    Optional<Menu> findMenu = menuRepository.findByMenuNameAndPostId(mainMenu, post.getId());
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
        Post post = postRepository.findById(postId).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_POST));
        List<Follow> followingsList = followRepository.findByToMemberId(memberId);

        // 포스트의 권한 설정이 PRIVATE인 경우
        if (post.getPermission() == Permission.PRIVATE) {
            if (!post.getMember().getId().equals(member.getId())) {
                throw new CustomException(ErrorCode.FORBIDDEN_MEMBER);
            }
        }

        // 포스트의 권한 설정이 FRIENDS인 경우
        if (post.getPermission() == Permission.FRIENDS) {
            boolean isFollower = followingsList.stream()
                    .anyMatch(follow -> follow.getFromMember().getId().equals(post.getMember().getId()));
            if (!isFollower && !post.getMember().getId().equals(member.getId())) {
                throw new CustomException(ErrorCode.FORBIDDEN_MEMBER);
            }
        }

        return new PostResponse(post);
    }

    // 포스트 수정
    @Transactional
    public PostResponse updatePost(Long postId, List<MultipartFile> images, PostUpdateRequest postUpdateRequest, Long memberId) throws IOException {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));
        Post post = postRepository.findByIdAndMemberId(postId, member.getId()).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_POST));
        // todo: 포스트, 회원 검증 로직 추가 -> 위의 내용 삭제

        // 전체에 있어 식당은 하나만 저장한다.
        // 사용자가 이미 한번 생성한 식당은 포스트를 새로 생성할 수 없다.
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

            if (!store.getId().equals(post.getStore().getId())) {
                if (postRepository.existsByStoreIdAndMemberId(store.getId(), memberId)) {
                    throw new CustomException(ErrorCode.DUPLICATE_POST_STORE);
                }
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
                menuRepository.save(menu);

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

        // 이미지 개수 = 메뉴 개수일 경우, 이미지 개수는 순서대로 Menu와 1:1 대응시켜 저장해준다.
        // 이미지 개수 != 메뉴 개수일 경우, 예외 처리 해준다.
        if (images == null || images.size() != menus.size()) {
            throw new CustomException(ErrorCode.IMAGE_NOT_EQUAL_WITH_MENU);
        }

        for (int i = 0; i < menus.size(); i++) {
            Menu menu = menus.get(i);
            menu.setPost(post);
            menuRepository.save(menu);

            // 새로운 이미지 업로드를 위해 기존 이미지 삭제
            MenuImage menuImage = menu.getMenuImage();

            if (menuImage != null) {
                s3ImageService.deleteImageFromS3(menuImage.getUrl());
            }

            MultipartFile image = images.get(i);
            // 수정 이미지로 재업로드
            BufferedImage resizedImage = s3ImageService.resizeImageWithAspectRatio(image, 512, 512);
            String url = s3ImageService.uploadImage(resizedImage, image.getOriginalFilename());

            menuImage.updateMenuImage(url);
            menuImageRepository.save(menuImage);
        }

        return new PostResponse(post);
    }

    // 포스트 삭제
    public void deletePost(Long postId, Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));
        Post post = postRepository.findByIdAndMemberId(postId, member.getId()).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_POST));
        // todo: 포스트, 회원 검증 로직 추가 -> 위의 내용 삭제

        List<Menu> menus = post.getMenus();
        for (Menu menu : menus) {
            String url = menu.getMenuImage().getUrl();
            s3ImageService.deleteImageFromS3(url);
        }
        postRepository.delete(post);
    }

    public PostUpdateCheckResponse checkUpdatePost(Long postId, Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));
        Post post = postRepository.findById(postId).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_POST));

        if (member.getId() != post.getMember().getId()) {
            return new PostUpdateCheckResponse(false);
        } else {
            return new PostUpdateCheckResponse(true);
        }
    }
}
