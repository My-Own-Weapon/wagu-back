package com.chimaera.wagubook.service;


import com.chimaera.wagubook.dto.response.LiveResponse;
import com.chimaera.wagubook.dto.response.StorePostResponse;
import com.chimaera.wagubook.dto.response.StoreResponse;

import com.chimaera.wagubook.entity.*;
import com.chimaera.wagubook.repository.liveRoom.LiveRoomRepository;

import com.chimaera.wagubook.repository.member.FollowRepository;
import com.chimaera.wagubook.repository.menu.MenuRepository;
import com.chimaera.wagubook.repository.post.PostRepository;
import com.chimaera.wagubook.repository.store.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.awt.print.Pageable;
import java.util.ArrayList;
import java.util.List;

import java.util.Optional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoreService {
    private final StoreRepository storeRepository;
    private final PostRepository postRepository;
    private final MenuRepository menuRepository;
    private final LiveRoomRepository liveRoomRepository;


    public List<StoreResponse> getStoresByScreen(String left, String right, String up, String down) {
        return storeRepository.findAllByScreen(left,right,up,down).stream()
                .map(store -> (new StoreResponse(store, (liveRoomRepository.findByStoreId(store.getId()).isEmpty()) ? false : true)))
                .collect(Collectors.toList());
    }
    
    public List<StorePostResponse> getAllPostsByStore(Long storeId, int page, int size, Long memberId) {


        return postRepository.findByStoreIdAndPage(memberId,storeId,page,size).stream()
                .map(post -> {
                    // 보내지는 정보는 사용자가 작성한 Main Menu 기준으로
                    // 일치하는 것이 없을 경우, 첫번째 menu를 보내주기
                    String mainMenu = post.getPostMainMenu();
                    Optional<Menu> menu = menuRepository.findByMenuNameAndPostId(mainMenu, post.getId());

                    if (menu.isPresent()) {
                        return new StorePostResponse(post, menu.get());
                    } else {
                        return new StorePostResponse(post, post.getMenus().get(0));
                    }
                })
                .collect(Collectors.toList());
    }


    public Store findByStoreLocationAndStoreName(Location storeLocation, String storeName) { 
        return storeRepository.findByStoreLocationAndStoreName(storeLocation, storeName).orElse(null);
    }

    public Store save(Store store){
        return storeRepository.save(store);
    }

    public List<LiveResponse> getLiveListByStore(Long storeId, Long memberId) {
        List<LiveRoom> liveRoomList = liveRoomRepository.findByStoreId(storeId);
        if(liveRoomList.isEmpty()){
            return new ArrayList<>();
        }

        return liveRoomList.stream()
                .map(liveRoom -> (new LiveResponse(liveRoom)))
                .collect(Collectors.toList());

    }
}