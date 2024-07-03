package com.chimaera.wagubook.controller;

import com.chimaera.wagubook.dto.PostResponse;
import com.chimaera.wagubook.dto.StoreResponse;
import com.chimaera.wagubook.entity.Post;
import com.chimaera.wagubook.entity.Store;
import com.chimaera.wagubook.exception.CustomException;
import com.chimaera.wagubook.exception.ErrorCode;
import com.chimaera.wagubook.service.StoreService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class StoreController {
    private final StoreService storeService;

    /**
     * 좌표에 맞는 식당 좌표 조회
     * Method : GET
     * url : /map?left={left}&right={right}&up={up}&down={down}
     * ex : map?left=1&right=20&up=1&down=20
     * */
    @GetMapping("/map")
    public ResponseEntity<List<StoreResponse>> findStores(
            @RequestParam(value = "left") String left,
            @RequestParam(value = "right") String right,
            @RequestParam(value = "up") String up,
            @RequestParam(value = "down") String down,
            HttpSession session){

        Long memberId = (Long) session.getAttribute("memberId");
        if (memberId == null) {
            throw new CustomException(ErrorCode.REQUEST_LOGIN);
        }

        List<Store> findStores = storeService.getStoresByScreen(left,right,up,down);
        List<StoreResponse> collect = findStores.stream()
                .map(s -> (new StoreResponse(s)))
                .collect(Collectors.toList());
        return new ResponseEntity<>(collect, HttpStatus.OK);
    }


    /**
     * 식당 이름, 주소로 포스트 조회
     * Method : GET
     * url : /map/posts?storeId={storeId}
     * */
    @GetMapping("/map/posts")
    public ResponseEntity<List<PostResponse>> getPostsByStore(
            @RequestParam(value = "storeId") Long storeId,
            HttpSession session){

        Long memberId = (Long) session.getAttribute("memberId");
        if (memberId == null) {
            throw new CustomException(ErrorCode.REQUEST_LOGIN);
        }

        List<Post> findPosts = storeService.getAllPostsByStore(storeId);
        List<PostResponse> collect = findPosts.stream()
                .map(p -> (new PostResponse(p.getId(),
                        p.getPostMainMenu(),
                        p.getPostImage(),
                        p.getPostContent(),
                        p.isAuto())))
                .collect(Collectors.toList());
        return new ResponseEntity<>(collect, HttpStatus.OK);
    }
}
