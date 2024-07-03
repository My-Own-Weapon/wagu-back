package com.chimaera.wagubook.service;

import com.chimaera.wagubook.dto.LoginRequest;
import com.chimaera.wagubook.dto.MemberRequest;
import com.chimaera.wagubook.entity.Follow;
import com.chimaera.wagubook.entity.Member;
import com.chimaera.wagubook.exception.CustomException;
import com.chimaera.wagubook.exception.ErrorCode;
import com.chimaera.wagubook.repository.member.FollowRepository;
import com.chimaera.wagubook.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final FollowRepository followRepository;
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

    public void updateProfileImage(Long memberId, String image) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));
        member.setProfileImage(image);
        memberRepository.save(member);
    }

    public void updatePassword(Long memberId, String newPassword) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));
        member.setPassword(bCryptPasswordEncoder.encode(newPassword));
        memberRepository.save(member);
    }

    public void deleteMember(Long memberId) {
        memberRepository.deleteById(memberId);
    }

    // 회원 팔로우 추가
    public void createFollow(Long followingId, Long followerId) {
        Member following = memberRepository.findById(followingId).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));
        Member follower = memberRepository.findById(followerId).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));

        if (followRepository.existsByFollowerIdAndFollowingId(followerId, followingId)) {
            throw new CustomException(ErrorCode.ALREADY_FOLLOW);
        }

        Follow follow = Follow.builder()
                .follower(follower)
                .following(following)
                .build();

        followRepository.save(follow);
    }
}
