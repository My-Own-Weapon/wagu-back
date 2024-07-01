package com.chimaera.wagubook.service;

import com.chimaera.wagubook.dto.LoginRequest;
import com.chimaera.wagubook.dto.MemberRequest;
import com.chimaera.wagubook.entity.Member;
import com.chimaera.wagubook.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;


    // 회원가입
    public void join(MemberRequest request) {
        Member user = Member.builder()
                .username(request.getUsername())
                .password(bCryptPasswordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .phoneNumber(request.getPhoneNumber())
                .profileImage(request.getProfileImage())
                .build();
        // 중복 회원 검증
        Optional<Member> findUser = memberRepository.findByUsername(user.getUsername());
        if (findUser.isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 회원입니다.");
        }
        memberRepository.save(user);
    }

    // 로그인
    public Member login(LoginRequest request) {
        String username = request.getUsername();
        Optional<Member> getUser = memberRepository.findByUsername(username);

        if (getUser.isPresent()) {
            Member member = getUser.get();
            if (bCryptPasswordEncoder.matches(request.getPassword(), member.getPassword())) {
                return member;
            }
        }
        return null;
    }

    public Member findByUsername(String username) {
        return memberRepository.findByUsername(username).orElse(null);
    }

    public void updateProfileImage(Long userId, String image) {
        Member member = memberRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("유효하지 않은 사용자 ID입니다."));
        member.setProfileImage(image);
        memberRepository.save(member);
    }

    public void updatePassword(Long userId, String newPassword) {
        Member member = memberRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("유효하지 않은 사용자 ID입니다."));
        member.setPassword(bCryptPasswordEncoder.encode(newPassword));
        memberRepository.save(member);
    }

    public void deleteMember(Long userId) {
        memberRepository.deleteById(userId);
    }
}
