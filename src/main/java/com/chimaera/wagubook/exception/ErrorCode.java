package com.chimaera.wagubook.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public class ErrorCode {
    // 400 BAD_REQUEST: 잘못된 요청


    // 401 UNAUTHORIZED: 인증되지 않은 사용자


    // 403 FORBIDDEN: 접근 권한이 없는 사용자


    // 404 NOT_FOUND: 잘못된 리소스 접근


    // 409 CONFLICT: 중복된 리소스 (요청이 현재 서버 상태와 충돌될 때)


    // 500 INTERNAL SERVER ERROR
    ;


    private final HttpStatus httpStatus;
    private final String message;
}