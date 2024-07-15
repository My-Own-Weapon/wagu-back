package com.chimaera.wagubook.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionAdvice {

    // CustomException: Error Code에 정의된 비즈니스 로직 오류
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
        ErrorCode errorCode = e.getErrorCode();
        return getErrorResponse(e, errorCode.getHttpStatus());
    }

    /*
        BAD_REQUEST (400)
        IllegalArgumentException: 사용자가 값을 잘못 입력한 경우
        MethodArgumentNotValidException: 전달된 값이 유효하지 않은 경우
        HttpMessageNotReadableException: 잘못된 형식으로 요청할 경우
        MissingServletRequestParameterException: 필수 요청 매개변수가 누락된 경우
    */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        return getErrorResponse(e, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();

        ArrayList<String> arrayList = new ArrayList<>();
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            arrayList.add(fieldError.getDefaultMessage());
        }

        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                String.join(", ", arrayList)
        );

        logError(e, HttpStatus.BAD_REQUEST);

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        return getErrorResponse(e, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        return getErrorResponse(e, HttpStatus.BAD_REQUEST);
    }

    /*
        METHOD_NOT_ALLOWED (405)
        HttpRequestMethodNotSupportedException: 잘못된 Http Method를 가지고 요청할 경우
    */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        return getErrorResponse(e, HttpStatus.METHOD_NOT_ALLOWED);

    }

    /*
        INTERNAL_SERVER_ERROR (500)
        RuntimeException
    */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException e) {
        return getErrorResponse(e, HttpStatus.INTERNAL_SERVER_ERROR);

    }

    // 예상하지 못한 모든 예외를 처리
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        return getErrorResponse(e, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // 추후 자주 발생하는 오류에 대해 추가

    // 공통 로직
    private ResponseEntity<ErrorResponse> getErrorResponse(Exception e, HttpStatus httpStatus) {
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                httpStatus.value(),
                httpStatus.getReasonPhrase(),
                e.getMessage()
        );

        logError(e, httpStatus);

        return new ResponseEntity<>(errorResponse, httpStatus);
    }

    private void logError(Exception e, HttpStatus httpStatus) {
        log.error("Exception: {} time: {} ErrorCode: {} Message: {}",
                e.getClass().getSimpleName(), LocalDateTime.now(), httpStatus.getReasonPhrase(), e.getMessage());
    }
}
