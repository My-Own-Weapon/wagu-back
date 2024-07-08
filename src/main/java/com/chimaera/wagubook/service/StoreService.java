package com.chimaera.wagubook.service;

import com.chimaera.wagubook.dto.StorePostResponse;
import com.chimaera.wagubook.dto.StoreResponse;
import com.chimaera.wagubook.entity.Menu;
import com.chimaera.wagubook.entity.Post;
import com.chimaera.wagubook.repository.menu.MenuRepository;
import com.chimaera.wagubook.repository.post.PostRepository;
import com.chimaera.wagubook.repository.store.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoreService {
    private final StoreRepository storeRepository;
    private final PostRepository postRepository;
    private final MenuRepository menuRepository;

    public List<StoreResponse> getStoresByScreen(String left, String right, String up, String down) {
        return storeRepository.findAllByScreen(left,right,up,down).stream()
                .map(store -> (new StoreResponse(store)))
                .collect(Collectors.toList());
    }
    
    public List<StorePostResponse> getAllPostsByStore(Long storeId) {
        return postRepository.findAllByStoreId(storeId).stream()
                .filter(Post::isFinished)
                .filter(post -> post.getPostMainMenu() != null)
                .map(post -> {
                    // 보내지는 정보는 사용자가 작성한 Main Menu 기준으로
                    //todo: mainMenu와 menu 객체의 menuName이 일치하지 않은 경우 고려하기
                    String mainMenu = post.getPostMainMenu();
                    Menu menu = menuRepository.findByMenuName(mainMenu);

                    return new StorePostResponse(post.getId(), mainMenu, menu.getMenuImage(), menu.getMenuContent());
                })
                .collect(Collectors.toList());
    }
}