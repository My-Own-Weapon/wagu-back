package com.chimaera.wagubook.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@AllArgsConstructor
@Getter
public enum ErrorCode {
    // 400 BAD_REQUEST: 잘못된 요청
    UNABLE_TO_UPDATE_POST(BAD_REQUEST, "포스트를 수정할 수 없습니다."),
    UNABLE_TO_DELETE_POST(BAD_REQUEST, "포스트를 삭제할 수 없습니다."),


    // 401 UNAUTHORIZED: 인증되지 않은 사용자
    REQUEST_LOGIN(UNAUTHORIZED, "로그인이 필요합니다."),
    LOGIN_FAIL(UNAUTHORIZED, "로그인이 실패했습니다."),

    // 403 FORBIDDEN: 접근 권한이 없는 사용자


    // 404 NOT_FOUND: 잘못된 리소스 접근
    NOT_FOUND_POST(NOT_FOUND, "해당 포스트를 찾을 수 없습니다."),
    NOT_FOUND_MEMBER(NOT_FOUND, "해당 회원을 찾을 수 없습니다."),


    // 409 CONFLICT: 중복된 리소스 (요청이 현재 서버 상태와 충돌될 때)
    ALREADY_FOLLOW(CONFLICT, "이미 팔로우한 회원입니다."),

    // 500 INTERNAL SERVER ERROR
    ;


    private final HttpStatus httpStatus;
    private final String message;
}