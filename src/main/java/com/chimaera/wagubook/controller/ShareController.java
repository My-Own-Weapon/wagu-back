package com.chimaera.wagubook.controller;

import com.chimaera.wagubook.dto.ShareResponse;
import com.chimaera.wagubook.exception.CustomException;
import com.chimaera.wagubook.exception.ErrorCode;
import com.chimaera.wagubook.service.ShareService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ShareController {
    private final ShareService shareService;

    /**
     * 랜덤 url 생성
     * Method : POST
     * url : members/share
     * */
    @PostMapping("/share")
    public ResponseEntity<ShareResponse> createUrl(HttpSession session){

        Long memberId = (Long) session.getAttribute("memberId");
        if (memberId == null) {
            throw new CustomException(ErrorCode.REQUEST_LOGIN);
        }

        return new ResponseEntity<>(shareService.createUrl(), HttpStatus.OK);
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

        return new ResponseEntity<>(shareService.findShareId(url), HttpStatus.OK);
    }


//    @GetMapping("/share/{share_id}/map")


}
