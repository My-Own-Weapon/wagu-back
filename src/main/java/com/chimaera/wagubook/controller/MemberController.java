package com.chimaera.wagubook.controller;

import com.chimaera.wagubook.dto.LoginRequest;
import com.chimaera.wagubook.dto.MemberRequest;
import com.chimaera.wagubook.entity.Member;
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
        if (member == null) {
            return new ResponseEntity<>("로그인 실패", HttpStatus.UNAUTHORIZED);
        }
        HttpSession session = request.getSession();
        session.setAttribute("userId", member.getId());
        session.setMaxInactiveInterval(3000); // 세션 유효 시간 50분
        return new ResponseEntity<>("로그인 성공", HttpStatus.OK);
    }

    @GetMapping("/join/username")
    public ResponseEntity<Boolean> checkUsername(@RequestParam String username) {
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
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return new ResponseEntity<>("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED);
        }
        memberService.updateProfileImage(userId, image);
        return new ResponseEntity<>("프로필 사진이 변경되었습니다.", HttpStatus.OK);
    }

    @PatchMapping("/members/password")
    public ResponseEntity<String> updatePassword(@RequestBody String newPassword, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return new ResponseEntity<>("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED);
        }
        memberService.updatePassword(userId, newPassword);
        return new ResponseEntity<>("비밀번호가 변경되었습니다.", HttpStatus.OK);
    }

    @DeleteMapping("/members")
    public ResponseEntity<String> deleteMember(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return new ResponseEntity<>("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED);
        }
        memberService.deleteMember(userId);
        session.invalidate();
        return new ResponseEntity<>("회원 탈퇴가 완료되었습니다.", HttpStatus.OK);
    }
}