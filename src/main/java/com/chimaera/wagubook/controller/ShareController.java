package com.chimaera.wagubook.controller;

import com.chimaera.wagubook.dto.FriendResponse;
import com.chimaera.wagubook.dto.ShareResponse;
import com.chimaera.wagubook.dto.StoreResponse;
import com.chimaera.wagubook.entity.Store;
import com.chimaera.wagubook.exception.CustomException;
import com.chimaera.wagubook.exception.ErrorCode;
import com.chimaera.wagubook.service.ShareService;
import com.chimaera.wagubook.service.StoreService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class ShareController {
    private final ShareService shareService;
    private final StoreService storeService;
    /**
     * 랜덤 url 생성
     * Method : POST
     * url : members/share
     * */
    @PostMapping("/share")
    public ResponseEntity<String> createUrl(HttpSession session){

        Long memberId = (Long) session.getAttribute("memberId");
        if (memberId == null) {
            throw new CustomException(ErrorCode.REQUEST_LOGIN);
        }

        return new ResponseEntity<>(shareService.createUrl(memberId), HttpStatus.OK);
    }


    /**
     * 랜덤 url로 접근 했을 때 share_id를 반환 ++++ 사용할 수 없으면 null 반환
     * Method : GET
     * url : /share/{random_url}
     * ex : /share/wv1Vpc4Eqt
     */
    @GetMapping("/share/{url}")
    public ResponseEntity<String> findShareId(@PathVariable String url, HttpSession session){

        Long memberId = (Long) session.getAttribute("memberId");
        if (memberId == null) {
            throw new CustomException(ErrorCode.REQUEST_LOGIN);
        }

        return new ResponseEntity<>(shareService.findShareId(url, memberId), HttpStatus.OK);
    }


    /**
     * url-> share_id를 통해 들어왔을 때 표시되는 지도
     * Method : GET
     * url : /share/{share_id}/map?left={left}&right={right}&up={up}&down={down}
     * ex : /share/2/map?left=1&right=20&up=1&down=20
     */
    @GetMapping("/share/{share_id}/map")
    public ResponseEntity<List<StoreResponse>> findStores(
            @PathVariable String share_id,
            @RequestParam(value = "left") String left,
            @RequestParam(value = "right") String right,
            @RequestParam(value = "up") String up,
            @RequestParam(value = "down") String down,
            HttpSession session){

        Long memberId = (Long) session.getAttribute("memberId");
        if (memberId == null) {
            throw new CustomException(ErrorCode.REQUEST_LOGIN);
        }

        // 화면에 보이는 스토어 찾기
        List<StoreResponse> findStores = storeService.getStoresByScreen(left,right,up,down);

        return new ResponseEntity<>(findStores, HttpStatus.OK);
    }

    /**
     * 투표 추가 기능
     * url : /share/{share_id}?store_id={store_id}
     * */
    @PostMapping("/share/{share_id}")
    public ResponseEntity<String> addVoteStore(
            @PathVariable String share_id,
            @RequestParam String store_id,
            HttpSession session){

        Long memberId = (Long) session.getAttribute("memberId");
        if (memberId == null) {
            throw new CustomException(ErrorCode.REQUEST_LOGIN);
        }

        Store findStore = storeService.findByStoreId(store_id);
        return new ResponseEntity<>("투표에 추가되었습니다.", HttpStatus.OK);
    }

    /**
     * 투표 삭제 기능
     * url : /share/{share_id}?store={store_name}
     * */
    @DeleteMapping("/share/{share_id}")
    public ResponseEntity<String> deleteVoteStore(){

    }

    /**
     * 투표 좋아요
     * url : /share/{share_id}/stores/{store_id}
     * */
    @PostMapping("/share/{share_id}/")
    public ResponseEntity<String> like(){

    }


    /**
     * 투표 좋아요 취소
     * url : /share/{share_id}/stores/{store_id}
     * */
    @PatchMapping("/share/{share_id}")
    public ResponseEntity<String> likeCancel(){

    }

    /**
     * 투표 결과 보기
     * url : /share/{share_id}/result
     * */
    @GetMapping("/share/{share_id}/result")
    public ResponseEntity<String> showResult(){

    }

}
