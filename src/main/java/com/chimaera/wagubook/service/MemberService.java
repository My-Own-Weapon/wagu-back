package com.chimaera.wagubook.service;

import com.chimaera.wagubook.dto.*;
import com.chimaera.wagubook.entity.Follow;
import com.chimaera.wagubook.entity.Member;
import com.chimaera.wagubook.entity.MemberImage;
import com.chimaera.wagubook.exception.CustomException;
import com.chimaera.wagubook.exception.ErrorCode;
import com.chimaera.wagubook.repository.member.FollowRepository;
import com.chimaera.wagubook.repository.member.MemberImageRepository;
import com.chimaera.wagubook.repository.member.MemberRepository;
import com.chimaera.wagubook.repository.post.PostRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final FollowRepository followRepository;
    private final PostRepository postRepository;
    private final MemberImageRepository memberImageRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final S3ImageService s3ImageService;


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
    public MemberResponse login(HttpServletRequest httpServletRequest, LoginRequest loginRequest) {
        String username = loginRequest.getUsername();
        Optional<Member> getUser = memberRepository.findByUsername(username);

        if (getUser.isPresent()) {
            Member member = getUser.get();
            if (bCryptPasswordEncoder.matches(loginRequest.getPassword(), member.getPassword())) {
                HttpSession session = httpServletRequest.getSession();

                session.setAttribute("memberId", member.getId());
                session.setMaxInactiveInterval(3000); // 세션 유효 시간 50분

                return new MemberResponse(member);
            }
        }

        return null;
    }

    // 아이디 중복 확인
    public Member findByUsername(String username) {
        return memberRepository.findByUsername(username).orElseThrow(() -> null);
    }

    // 프로필 사진 변경
    public MemberResponse updateMemberImage(Long memberId, MultipartFile image) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));
        Optional<MemberImage> findMemberImage = memberImageRepository.findByMemberId(memberId);

        if (findMemberImage.isPresent()) {
            MemberImage memberImage = findMemberImage.get();
            s3ImageService.deleteImageFromS3(memberImage.getUrl());
            String url = s3ImageService.upload(image);
            memberImage.updateProfileImage(url);
            memberImageRepository.save(memberImage);
        } else {
            String url = s3ImageService.upload(image);
            MemberImage memberImage = MemberImage.newBuilder()
                    .url(url)
                    .member(member)
                    .build();
            memberImageRepository.save(memberImage);
        }

        memberRepository.save(member);

        return new MemberResponse(member);
    }

    // 비밀번호 변경
    public void updatePassword(Long memberId, String newPassword) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));
        member.updatePassword(bCryptPasswordEncoder.encode(newPassword));
        memberRepository.save(member);
    }

    // 회원 탈퇴
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
            oppositeFollow.updateEach(true);
        }

        Follow follow = Follow.newBuilder()
                .toMember(toMember)
                .fromMember(fromMember)
                .isEach(isEach)
                .build();

        followRepository.save(follow);
    }

    // 회원 팔로우 삭제
    public void deleteFollow(Long toMemberId, Long fromMemberId) {
        Member toMember = memberRepository.findById(toMemberId).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));
        Member fromMember = memberRepository.findById(fromMemberId).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));
        Follow follow = followRepository.findByToMemberIdAndFromMemberId(toMember.getId(), fromMember.getId()).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_FOLLOW));

        followRepository.delete(follow);
    }

    // 팔로우 목록 조회
    public List<FollowerResponse> getFollowers(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));
        return followRepository.findByFromMemberId(member.getId()).stream()
                .map(FollowerResponse::new)
                .collect(Collectors.toList());
    }

    // 팔로잉 목록 조회
    public List<FollowingResponse> getFollowings(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));
        return followRepository.findByToMemberId(member.getId()).stream()
                .map(FollowingResponse::new)
                .collect(Collectors.toList());
    }

    // 프로필 조회
    public MemberInfoResponse getMemberInfo(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));
        int followerNum = followRepository.countByFromMemberId(member.getId());
        int followingNum = followRepository.countByToMemberId(member.getId());
        int postNum = postRepository.countByMemberId(member.getId());
        return new MemberInfoResponse(followerNum, followingNum, postNum);
    }
}
