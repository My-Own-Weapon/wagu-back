package com.chimaera.wagubook.controller;

import com.chimaera.wagubook.dto.response.StoreResponse;
import com.chimaera.wagubook.dto.response.StoreSearchResponse;
import com.chimaera.wagubook.exception.CustomException;
import com.chimaera.wagubook.exception.ErrorCode;
import com.chimaera.wagubook.service.ShareService;
import com.chimaera.wagubook.service.StoreService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    @Operation(summary = "랜덤 url 생성")
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
    @Operation(summary = "랜덤 url로 접근 했을 때 share_id를 반환/ 사용할 수 없으면 null 반환")
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
    @Operation(summary = "share_id를 통해 들어왔을 때 표시되는 지도")
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
     * 투표에 추가 기능
     * url : /share/{share_id}?store_id={store_id}
     * */
    @PostMapping("/share/{url}")
    @Operation(summary = "투표에 추가 기능")
    public ResponseEntity<String> addVoteStore(
            @PathVariable String url,
            @RequestParam String store_id,
            HttpSession session){

        Long memberId = (Long) session.getAttribute("memberId");
        if (memberId == null) {
            throw new CustomException(ErrorCode.REQUEST_LOGIN);
        }

        return new ResponseEntity<>(shareService.addVoteStore(url,store_id), HttpStatus.OK);
    }

    /**
     * 투표에서 삭제 기능
     * url : /share/{url}?store={store_id}
     * */
    @DeleteMapping("/share/{url}")
    @Operation(summary = "투표에서 삭제 기능")
    public ResponseEntity<String> deleteVoteStore(
            @PathVariable String url,
            @RequestParam String store_id,
            HttpSession session){

        Long memberId = (Long) session.getAttribute("memberId");
        if (memberId == null) {
            throw new CustomException(ErrorCode.REQUEST_LOGIN);
        }

        return new ResponseEntity<>(shareService.deleteVoteStore(url,store_id), HttpStatus.OK);
    }

    /**
     * 투표 좋아요
     * url : /share/{url}/vote?store={store_id}
     * */
    @PostMapping("/share/{url}/vote")
    @Operation(summary = "투표 좋아요")
    public ResponseEntity<String> like(@PathVariable String url, @RequestParam String store_id,HttpSession session) throws InterruptedException{

        Long memberId = (Long) session.getAttribute("memberId");
        if (memberId == null) {
            throw new CustomException(ErrorCode.REQUEST_LOGIN);
        }

        return new ResponseEntity<>(shareService.like(url, store_id), HttpStatus.OK);
    }


    /**
     * 투표 좋아요 취소
     * url : /share/{url}/vote?store={store_id}
     * */
    @PatchMapping("/share/{url}/vote")
    @Operation(summary = "투표 좋아요 취소")
    public ResponseEntity<String> likeCancel(@PathVariable String url, @RequestParam String store_id,HttpSession session){

        Long memberId = (Long) session.getAttribute("memberId");
        if (memberId == null) {
            throw new CustomException(ErrorCode.REQUEST_LOGIN);
        }

        return new ResponseEntity<>(shareService.likeCancel(url, store_id), HttpStatus.OK);
    }

    /**
     * 투표 결과 보기
     * url : /share/{url}/result
     * */
    @GetMapping("/share/{url}/result")
    @Operation(summary = "투표 결과 보기")
    public ResponseEntity<List<StoreResponse>> showResult(@PathVariable String url,HttpSession session){

        Long memberId = (Long) session.getAttribute("memberId");
        if (memberId == null) {
            throw new CustomException(ErrorCode.REQUEST_LOGIN);
        }

        return new ResponseEntity<>(shareService.showResult(url),HttpStatus.OK);
    }

    /**
     * 투표 리스트 조회
     * url : /share/{url}/vote/list
     * */
    @GetMapping("/share/{url}/vote/list")
    @Operation(summary = "투표 리스트 조회")
    public ResponseEntity<List<StoreSearchResponse>> showVoteList(@PathVariable String url, HttpSession session){

        Long memberId = (Long) session.getAttribute("memberId");
        if (memberId == null) {
            throw new CustomException(ErrorCode.REQUEST_LOGIN);
        }

        return new ResponseEntity<>(shareService.showVoteList(url),HttpStatus.OK);
    }

}
