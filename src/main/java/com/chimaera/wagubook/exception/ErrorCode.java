package com.chimaera.wagubook.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@AllArgsConstructor
@Getter
public enum ErrorCode {
    // 400 BAD_REQUEST: 잘못된 요청
    NOT_ALLOW_FOLLOW(BAD_REQUEST, "허용되지 않는 팔로우입니다."),
    WRONG_PASSWORD_CONFIRM(BAD_REQUEST, "비밀번호를 다시 확인해주세요."),
    EMPTY_FILE(BAD_REQUEST, "요청된 파일이 없습니다."),
    NO_FILE_EXTENSION(BAD_REQUEST, "확장자가 없는 파일입니다."),
    INVALID_FILE_EXTENSION(BAD_REQUEST, "허용되지 않는 확장자입니다."),
    FAIL_TO_UPLOAD_IMAGE(BAD_REQUEST, "이미지 업로드에 실패했습니다."),
    IO_EXCEPTION_ON_IMAGE_UPLOAD(BAD_REQUEST, "파일에 입출력 문제가 발생하였습니다."),
    IO_EXCEPTION_ON_IMAGE_DELETE(BAD_REQUEST, "파일에 입출력 문제가 발생하였습니다."),
    IMAGE_NOT_EQUAL_WITH_MENU(BAD_REQUEST, "허용되는 이미지 업로드 개수가 아닙니다."),
    WRONG_NAME(CONFLICT, "이름은 한글로만 입력 가능합니다."),
    WRONG_PHONE_NUMBER(CONFLICT, "휴대폰 번호는 숫자로만 입력 가능합니다."),
    WRONG_PASSWORD(CONFLICT, "비밀번호는 영문, 숫자, 특수문자 포함 8자 이상입니다."),
    OVER_MAX(BAD_REQUEST, "최대 10개까지만 투표에 추가할 수 있습니다."),
    ALREADY_ADD(BAD_REQUEST, "이미 추가된 가게입니다."),

    
    // 401 UNAUTHORIZED: 인증되지 않은 사용자
    REQUEST_LOGIN(UNAUTHORIZED, "로그인이 필요합니다."),
    LOGIN_FAIL(UNAUTHORIZED, "로그인이 실패했습니다."),

    // 403 FORBIDDEN: 허용되지 않은 접근
    FORBIDDEN_MEMBER(FORBIDDEN, "허용되지 않은 사용자입니다."),


    // 404 NOT_FOUND: 잘못된 리소스 접근
    NOT_FOUND_POST(NOT_FOUND, "해당 포스트를 찾을 수 없습니다."),
    NOT_FOUND_MEMBER(NOT_FOUND, "해당 회원을 찾을 수 없습니다."),
    NOT_FOUND_FOLLOW(NOT_FOUND, "성립되지 않은 팔로우 관계입니다."),
    NOT_FOUND_SHARE(NOT_FOUND, "공유방을 찾을 수 없습니다."),
    NOT_FOUND_STORE(NOT_FOUND, "해당 스토어를 찾을 수 없습니다."),
    NOT_FOUND_URL(NOT_FOUND, "해당 url이 존재하지 않습니다."),
    NOT_FOUND_LIVE_ROOM(NOT_FOUND, "해당 라이브룸을 찾을 수 없습니다."),
    SESSION_ROOM_NOT_FOUND(NOT_FOUND, "해당 세션룸을 찾을 수 없습니다."),



    // 409 CONFLICT: 중복된 리소스 (요청이 현재 서버 상태와 충돌될 때)

    DUPLICATE_USERNAME(CONFLICT, "중복된 아이디입니다."),
    ALREADY_FOLLOW(CONFLICT, "해당 사용자가 이미 팔로우한 회원입니다."),
    DUPLICATE_POST_MENU(CONFLICT, "이미 해당 포스트에 작성된 메뉴 이름입니다."),
    DUPLICATE_POST_STORE(CONFLICT, "이미 해당 사용자가 포스트를 작성한 식당입니다."),

    // 500 INTERNAL SERVER ERROR
    ;


    private final HttpStatus httpStatus;
    private final String message;
}
