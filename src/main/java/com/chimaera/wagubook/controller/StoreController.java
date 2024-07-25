package com.chimaera.wagubook.controller;

import com.chimaera.wagubook.dto.response.LiveResponse;
import com.chimaera.wagubook.dto.response.StorePostResponse;
import com.chimaera.wagubook.dto.response.StoreResponse;
import com.chimaera.wagubook.exception.CustomException;
import com.chimaera.wagubook.exception.ErrorCode;
import com.chimaera.wagubook.service.StoreService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class StoreController {
    private final StoreService storeService;

    /**
     * 좌표에 맞는 식당 좌표 조회
     * Method : GET
     * url : /map?left={left}&right={right}&up={up}&down={down}
     * ex : map?left=1&right=20&up=1&down=20
     * */
    // TODO: 여기서 해당 좌표 내부에 있는 식당 중 방송이 진행 중인지에 대한 boolean 값도 return.
    @GetMapping("/map")
    @Operation(summary = "좌표에 맞는 식당 좌표 조회")
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


        return new ResponseEntity<>(storeService.getStoresByScreen(left,right,up,down), HttpStatus.OK);

    }


    /**
     * 식당 아이디로 포스트 조회
     * Method : GET
     * url : /map/posts?storeId={storeId}&page={page}&size={size}
     * */
    @GetMapping("/map/posts")
    @Operation(summary = "식당 아이디로 포스트 조회")
    public ResponseEntity<List<StorePostResponse>> getPostsByStore(
            @RequestParam(value = "storeId") Long storeId,
            @RequestParam(value = "page") int page,
            @RequestParam(value = "size") int size,
            HttpSession session){

        Long memberId = (Long) session.getAttribute("memberId");
        if (memberId == null) {
            throw new CustomException(ErrorCode.REQUEST_LOGIN);
        }

        return new ResponseEntity<>(storeService.getAllPostsByStore(storeId,page,size,memberId), HttpStatus.OK);
    }

    /**
     * 식당 아이디로 라이브 리스트 조회
     * Method : GET
     * url : /map/posts?storeId={storeId}&page={page}&size={size}
     * */
    @GetMapping("/map/live")
    @Operation(summary = "식당 아이디로 라이브 리스트 조회")
    public ResponseEntity<List<LiveResponse>> getLiveByStore(
            @RequestParam(value = "storeId") Long storeId,
            HttpSession session){

        Long memberId = (Long) session.getAttribute("memberId");
        if (memberId == null) {
            throw new CustomException(ErrorCode.REQUEST_LOGIN);
        }

        return new ResponseEntity<>(storeService.getLiveListByStore(storeId,memberId), HttpStatus.OK);
    }
}
