package com.chimaera.wagubook.controller;

import com.chimaera.wagubook.dto.*;
import com.chimaera.wagubook.exception.CustomException;
import com.chimaera.wagubook.exception.ErrorCode;
import com.chimaera.wagubook.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    /**
     * 회원 가입
     * Method : POST
     * url : /join
     * */
    @PostMapping("/join")
    @Operation(summary = "회원 가입")
    public ResponseEntity<String> join(@RequestBody MemberRequest request) {
        memberService.join(request);
        return new ResponseEntity<>("회원가입을 성공하였습니다.", HttpStatus.CREATED);
    }

    /**
     * 로그인
     * Method : POST
     * url : /login
     * */
    @PostMapping("/login")
    @Operation(summary = "로그인")
    public ResponseEntity<MemberResponse> login(HttpServletRequest httpServletRequest, @RequestBody LoginRequest loginRequest) {
        MemberResponse memberResponse = memberService.login(httpServletRequest, loginRequest);

        if (memberResponse == null) {
            throw new CustomException(ErrorCode.LOGIN_FAIL);
        }

        return new ResponseEntity<>(memberResponse, HttpStatus.OK);
    }

    /**
     * 아이디 중복 확인
     * Method : GET
     * url : /join/username?username={username}
     * */
    @GetMapping("/join/username")
    @Operation(summary = "아이디 중복 확인")
    public ResponseEntity<Boolean> checkUsername(@RequestParam String username) {
        boolean exists = memberService.findByUsername(username) != null;
        return new ResponseEntity<>(exists, HttpStatus.OK);
    }

    /**
     * 로그아웃
     * Method : GET
     * url : /logout
     * */
    @GetMapping("/logout")
    @Operation(summary = "로그아웃")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return new ResponseEntity<>("로그아웃을 성공하였습니다.", HttpStatus.OK);
    }

    /**
     * 회원 프로필 사진 수정
     * Method : PATCH
     * url : /members/image
     * */
    @PatchMapping(value = "/members/image", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @Operation(summary = "회원 프로필 사진 수정")
    public ResponseEntity<MemberResponse> updateMemberImage(@RequestPart MultipartFile image, HttpSession session) {
        Long memberId = (Long) session.getAttribute("memberId");
        checkValidByMemberId(memberId);
        return new ResponseEntity<>(memberService.updateMemberImage(memberId, image), HttpStatus.OK);
    }

    /**
     * 회원 비밀번호 수정
     * Method : PATCH
     * url : /members/password
     * */
    @PatchMapping("/members/password")
    @Operation(summary = "회원 비밀번호 수정")
    public ResponseEntity<String> updatePassword(@RequestBody String newPassword, HttpSession session) {
        Long memberId = (Long) session.getAttribute("memberId");
        checkValidByMemberId(memberId);
        memberService.updatePassword(memberId, newPassword);
        return new ResponseEntity<>("비밀번호가 변경되었습니다.", HttpStatus.OK);
    }

    /**
     * 회원 탈퇴
     * Method : DELETE
     * url : /members
     * */
    @DeleteMapping("/members")
    @Operation(summary = "회원 탈퇴")
    public ResponseEntity<String> deleteMember(HttpSession session) {
        Long memberId = (Long) session.getAttribute("memberId");
        checkValidByMemberId(memberId);
        memberService.deleteMember(memberId);
        session.invalidate();
        return new ResponseEntity<>("회원 탈퇴가 완료되었습니다.", HttpStatus.OK);
    }

    /**
     * 회원 팔로우 추가
     * Method : POST
     * url : members/{fromMemberId}/follow
     * */
    @PostMapping("/members/{fromMemberId}/follow")
    @Operation(summary = "회원 팔로우 추가")
    public ResponseEntity<String> createFollow(@PathVariable Long fromMemberId, HttpSession session) {
        Long toMemberId = (Long) session.getAttribute("memberId");
        checkValidByMemberId(toMemberId);
        checkValidByMemberId(fromMemberId);
        memberService.createFollow(toMemberId, fromMemberId);
        return new ResponseEntity<>(toMemberId + "님이 " + fromMemberId + "님을 팔로우하였습니다.", HttpStatus.OK);
    }

    /**
     * 회원 팔로우 삭제
     * Method : DELETE
     * url : members/{fromMemberId}/follow
     * */
    @DeleteMapping("/members/{fromMemberId}/follow")
    @Operation(summary = "회원 팔로우 삭제")
    public ResponseEntity<String> deleteFollow(@PathVariable Long fromMemberId, HttpSession session) {
        Long toMemberId = (Long) session.getAttribute("memberId");
        checkValidByMemberId(toMemberId);
        checkValidByMemberId(fromMemberId);
        memberService.deleteFollow(toMemberId, fromMemberId);
        return new ResponseEntity<>(toMemberId + "님이 " + fromMemberId + "님을 언팔로우하였습니다.", HttpStatus.OK);
    }

    /**
     * 팔로워 목록 조회
     * Method : GET
     * url : /followers
     * */
    @GetMapping("/followers")
    @Operation(summary = "팔로워 목록 조회")
    public ResponseEntity<List<FollowerResponse>> getFollowers(HttpSession session) {
        Long memberId = (Long) session.getAttribute("memberId");
        checkValidByMemberId(memberId);
        List<FollowerResponse> followers = memberService.getFollowers(memberId);
        return new ResponseEntity<>(followers, HttpStatus.OK);
    }

    /**
     * 팔로잉 목록 조회
     * Method : GET
     * url : /followings
     * */
    @GetMapping("/followings")
    @Operation(summary = "팔로잉 목록 조회")
    public ResponseEntity<List<FollowingResponse>> getFollowings(HttpSession session) {
        Long memberId = (Long) session.getAttribute("memberId");
        checkValidByMemberId(memberId);
        return new ResponseEntity<>(memberService.getFollowings(memberId), HttpStatus.OK);
    }

    /**
     * 프로필 조회
     * Method : GET
     * url : /members/{memberId}
     * */
    @GetMapping("/members/{memberId}")
    @Operation(summary = "프로필 조회")
    public ResponseEntity<MemberInfoResponse> getMemberInfo(HttpSession session) {
        Long memberId = (Long) session.getAttribute("memberId");
        checkValidByMemberId(memberId);
        return new ResponseEntity<>(memberService.getMemberInfo(memberId), HttpStatus.OK);
    }

    // 회원 검증
    private void checkValidByMemberId(Long memberId) {
        if (memberId == null) {
            throw new CustomException(ErrorCode.REQUEST_LOGIN);
        }
    }
}