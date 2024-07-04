package com.chimaera.wagubook.controller;

import com.chimaera.wagubook.dto.ShareResponse;
import com.chimaera.wagubook.exception.CustomException;
import com.chimaera.wagubook.exception.ErrorCode;
import com.chimaera.wagubook.service.ShareService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ShareController {
    private final ShareService shareService;

    @PostMapping("/share")
    public ResponseEntity<ShareResponse> createUrl(HttpSession session){

        Long memberId = (Long) session.getAttribute("memberId");
        if (memberId == null) {
            throw new CustomException(ErrorCode.REQUEST_LOGIN);
        }

        return new ResponseEntity<>(shareService.createUrl(memberId), HttpStatus.OK);
    }
}
