package com.chimaera.wagubook.controller;

import com.chimaera.wagubook.dto.LoginRequest;
import com.chimaera.wagubook.dto.MemberRequest;
import com.chimaera.wagubook.entity.Member;
import com.chimaera.wagubook.exception.CustomException;
import com.chimaera.wagubook.exception.ErrorCode;
import com.chimaera.wagubook.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @PostMapping("/join")
    public ResponseEntity<String> join(@RequestBody MemberRequest request) {
        memberService.join(request);
        return new ResponseEntity<>("회원가입 성공", HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(HttpServletRequest request, @RequestBody LoginRequest loginRequest) {
        Member member = memberService.login(loginRequest);
        System.out.println("멤버: " + member);
        if (member == null) {
            throw new CustomException(ErrorCode.LOGIN_FAIL);
        }
        HttpSession session = request.getSession();
        session.setAttribute("memberId", member.getId());
        session.setAttribute("username", member.getUsername());
        session.setMaxInactiveInterval(300); // 세션 유효 시간 5분
        return new ResponseEntity<>("로그인 성공", HttpStatus.OK);
    }

    @PostMapping("/join/username")
    public ResponseEntity<Boolean> checkUsername(@RequestBody String username) {
        boolean exists = memberService.findByUsername(username) != null;
        return new ResponseEntity<>(exists, HttpStatus.OK);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return new ResponseEntity<>("로그아웃 성공", HttpStatus.OK);
    }

    @PatchMapping("/members/image")
    public ResponseEntity<String> updateProfileImage(@RequestBody String image, HttpSession session) {
        Long memberId = (Long) session.getAttribute("memberId");
        if (memberId == null) {
            throw new CustomException(ErrorCode.REQUEST_LOGIN);
        }
        memberService.updateProfileImage(memberId, image);
        return new ResponseEntity<>("프로필 사진이 변경되었습니다.", HttpStatus.OK);
    }

    @PatchMapping("/members/password")
    public ResponseEntity<String> updatePassword(@RequestBody String newPassword, HttpSession session) {
        Long memberId = (Long) session.getAttribute("memberId");
        if (memberId == null) {
            throw new CustomException(ErrorCode.REQUEST_LOGIN);
        }
        memberService.updatePassword(memberId, newPassword);
        return new ResponseEntity<>("비밀번호가 변경되었습니다.", HttpStatus.OK);
    }

    @DeleteMapping("/members")
    public ResponseEntity<String> deleteMember(HttpSession session) {
        Long memberId = (Long) session.getAttribute("userId");
        if (memberId == null) {
            throw new CustomException(ErrorCode.REQUEST_LOGIN);
        }
        memberService.deleteMember(memberId);
        session.invalidate();
        return new ResponseEntity<>("회원 탈퇴가 완료되었습니다.", HttpStatus.OK);
    }
}
