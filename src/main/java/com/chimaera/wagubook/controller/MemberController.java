package com.chimaera.wagubook.controller;

import com.chimaera.wagubook.dto.FollowerResponse;
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

import java.util.List;


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
            throw new CustomException(ErrorCode.LOGIN_FAIL);
        }
        HttpSession session = request.getSession();

        session.setAttribute("memberId", member.getId());
        session.setMaxInactiveInterval(3000); // 세션 유효 시간 50분

        return new ResponseEntity<>("로그인 성공", HttpStatus.OK);
    }

    @GetMapping("/join/username")
    public ResponseEntity<Boolean> checkUsername(@RequestParam String username) {
        boolean exists = memberService.findByUsername(username) != null;
        return new ResponseEntity<>(exists, HttpStatus.OK);
    }

    @GetMapping("/logout") //@PostMapping("/logout") 이 왜 안되는지 차후 논의 필요
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
        checkVaildByMemberId(memberId);
        memberService.updateProfileImage(memberId, image);
        return new ResponseEntity<>("프로필 사진이 변경되었습니다.", HttpStatus.OK);
    }

    @PatchMapping("/members/password")
    public ResponseEntity<String> updatePassword(@RequestBody String newPassword, HttpSession session) {
        Long memberId = (Long) session.getAttribute("memberId");
        checkVaildByMemberId(memberId);
        memberService.updatePassword(memberId, newPassword);
        return new ResponseEntity<>("비밀번호가 변경되었습니다.", HttpStatus.OK);
    }

    @DeleteMapping("/members")
    public ResponseEntity<String> deleteMember(HttpSession session) {
        Long memberId = (Long) session.getAttribute("memberId");
        checkVaildByMemberId(memberId);
        memberService.deleteMember(memberId);
        session.invalidate();
        return new ResponseEntity<>("회원 탈퇴가 완료되었습니다.", HttpStatus.OK);
    }

    /**
     * 회원 팔로우 추가
     * Method : POST
     * url : members/{fromMemberId}/follow
     * ex : members/5/follow
     **/
    @PostMapping("/members/{fromMemberId}/follow")
    public ResponseEntity<String> createFollow(@PathVariable Long fromMemberId, HttpSession session) {
        Long toMemberId = (Long) session.getAttribute("memberId");
        checkVaildByMemberId(toMemberId);
        checkVaildByMemberId(fromMemberId);
        memberService.createFollow(toMemberId, fromMemberId);
        return new ResponseEntity<>(toMemberId + "님이 " + fromMemberId + "님을 팔로우하였습니다.", HttpStatus.OK);
    }

    /**
     * 회원 팔로우 삭제
     * Method : DELETE
     * url : members/{fromMemberId}/follow
     * ex : members/5/follow
     **/
    @DeleteMapping("/members/{fromMemberId}/follow")
    public ResponseEntity<String> deleteFollow(@PathVariable Long fromMemberId, HttpSession session) {
        Long toMemberId = (Long) session.getAttribute("memberId");
        checkVaildByMemberId(toMemberId);
        checkVaildByMemberId(fromMemberId);
        memberService.deleteFollow(toMemberId, fromMemberId);
        return new ResponseEntity<>(toMemberId + "님이 " + fromMemberId + "님을 언팔로우하였습니다.", HttpStatus.OK);
    }

    /**
     * 팔로워 목록 조회
     * Method : GET
     * url : /followers
     * ex : /followers
     **/
    @GetMapping("/followers")
    public ResponseEntity<List<FollowerResponse>> getFollowers(HttpSession session) {
        Long memberId = (Long) session.getAttribute("memberId");


        return new ResponseEntity<List<FollowerResponse>>(HttpStatus.OK);
    }

    // 회원 검증
    private void checkVaildByMemberId(Long memberId) {
        if (memberId == null) {
            throw new CustomException(ErrorCode.REQUEST_LOGIN);
        }
    }
}