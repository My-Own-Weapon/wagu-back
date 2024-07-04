package com.chimaera.wagubook.service;

import com.chimaera.wagubook.dto.*;
import com.chimaera.wagubook.entity.Follow;
import com.chimaera.wagubook.entity.Member;
import com.chimaera.wagubook.exception.CustomException;
import com.chimaera.wagubook.exception.ErrorCode;
import com.chimaera.wagubook.repository.member.FollowRepository;
import com.chimaera.wagubook.repository.member.MemberRepository;
import com.chimaera.wagubook.repository.post.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final FollowRepository followRepository;
    private final PostRepository postRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;


    // 회원가입
    public void join(MemberRequest request) {
        if (!request.getPassword().equals(request.getPasswordConfirm())) {
            throw new CustomException(ErrorCode.WRONG_PASSWORD_CONFIRM);
        }

        Member user = Member.newBuilder()
                .username(request.getUsername())
                .password(bCryptPasswordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .phoneNumber(request.getPhoneNumber())
                .build();

        // 중복 회원 검증
        Optional<Member> findUser = memberRepository.findByUsername(user.getUsername());
        if (findUser.isPresent()) {
            throw new CustomException(ErrorCode.DUPLICATE_USERNAME);
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
        return memberRepository.findByUsername(username).orElseThrow(() -> new CustomException(ErrorCode.DUPLICATE_USERNAME));
    }

    public void updateProfileImage(Long memberId, String image) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));
        member.updateProfileImage(image);
        memberRepository.save(member);
    }

    public void updatePassword(Long memberId, String newPassword) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));
        member.updatePassword(bCryptPasswordEncoder.encode(newPassword));
        memberRepository.save(member);
    }

    public void deleteMember(Long memberId) {
        memberRepository.deleteById(memberId);
    }

    // 회원 팔로우 추가
    public void createFollow(Long toMemberId, Long fromMemberId) {
        Member toMember = memberRepository.findById(toMemberId).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));
        Member fromMember = memberRepository.findById(fromMemberId).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));

        // 사용자가 자기 자신을 팔로우하는 경우
        if (toMemberId.equals(fromMemberId)) {
            throw new CustomException(ErrorCode.NOT_ALLOW_FOLLOW);
        }
        
        // 이미 팔로우 관계가 성립되었을 경우
        if (followRepository.existsByToMemberIdAndFromMemberId(toMemberId, fromMemberId)) {
            throw new CustomException(ErrorCode.ALREADY_FOLLOW);
        }

        // 맞팔로우 여부 확인
        boolean isEach = false;
        Optional<Follow> findFollow = followRepository.findByToMemberIdAndFromMemberId(fromMemberId, toMemberId);

        if (findFollow.isPresent()) {
            isEach = true;

            Follow oppositeFollow = findFollow.get();
            oppositeFollow.update(true);
        }

        Follow follow = Follow.builder()
                .toMember(toMember)
                .fromMember(fromMember)
                .isEach(isEach)
                .build();

        followRepository.save(follow);
    }

    // 회원 팔로우 삭제
    public void deleteFollow(Long toMemberId, Long fromMemberId) {
        memberRepository.findById(toMemberId).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));
        memberRepository.findById(fromMemberId).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));
        Follow follow = followRepository.findByToMemberIdAndFromMemberId(toMemberId, fromMemberId).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_FOLLOW));

        followRepository.delete(follow);
    }

    // 팔로우 목록 조회
    public List<FollowerResponse> getFollowers(Long memberId) {
        memberRepository.findById(memberId).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));
        return followRepository.findByFromMemberId(memberId).stream()
                .map(FollowerResponse::new)
                .collect(Collectors.toList());
    }

    // 팔로잉 목록 조회
    public List<FollowingResponse> getFollowings(Long memberId) {
        memberRepository.findById(memberId).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));
        return followRepository.findByToMemberId(memberId).stream()
                .map(FollowingResponse::new)
                .collect(Collectors.toList());
    }

    // 프로필 조회
    public MemberInfoResponse getMemberInfo(Long memberId) {
        memberRepository.findById(memberId).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));
        int followerNum = followRepository.countByFromMemberId(memberId);
        int followingNum = followRepository.countByToMemberId(memberId);
        int postNum = postRepository.countByMemberId(memberId);
        return new MemberInfoResponse(followerNum, followingNum, postNum);
    }
}
