package com.chimaera.wagubook.service;

import com.chimaera.wagubook.dto.request.LoginRequest;
import com.chimaera.wagubook.dto.request.MemberRequest;
import com.chimaera.wagubook.dto.response.FollowerResponse;
import com.chimaera.wagubook.dto.response.FollowingResponse;
import com.chimaera.wagubook.dto.response.MemberInfoResponse;
import com.chimaera.wagubook.dto.response.MemberResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
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

    public Member findById(Long memberId) {
        return memberRepository.findById(memberId).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));
    }

    public void join(MemberRequest request) {
        if (!request.getPassword().equals(request.getPasswordConfirm())) {
            throw new CustomException(ErrorCode.WRONG_PASSWORD_CONFIRM);
        }

        Member member = Member.newBuilder()
                .username(request.getUsername())
                .password(bCryptPasswordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .phoneNumber(request.getPhoneNumber())
                .build();

        // 중복 회원 검증
        Optional<Member> findUser = memberRepository.findByUsername(member.getUsername());
        if (findUser.isPresent()) {
            throw new CustomException(ErrorCode.DUPLICATE_USERNAME);
        }

        // 비밀번호 검증: 영문, 숫자, 특수문자 포함 8자 이상
        validatePassword(request.getPassword());

        // 이름 검증: 한국어만 입력 가능
        validateName(member.getName());

        // 휴대폰 번호 검증: 숫자만 입력 가능
        validatePhoneNumber(member.getPhoneNumber());

        memberRepository.save(member);

        MemberImage memberImage = MemberImage.newBuilder()
                .member(member)
                .url(null)
                .build();

        member.updateMemberImage(memberImage);
        memberImageRepository.save(memberImage);
    }

    // 비밀번호 검증 메소드
    private void validatePassword(String password) {
        String passwordPattern = "^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[^a-zA-Z0-9]).{8,}$"; // 영문, 숫자, 특수문자 포함 8자 이상
        if (!password.matches(passwordPattern)) {
            throw new CustomException(ErrorCode.WRONG_PASSWORD);
        }
    }

    // 이름 검증 메소드
    private void validateName(String name) {
        String namePattern = "^[가-힣]*$";
        if (!name.matches(namePattern)) {
            throw new CustomException(ErrorCode.WRONG_NAME);
        }
    }

    // 휴대폰 번호 검증 메소드
    private void validatePhoneNumber(String phoneNumber) {
        String phonePattern = "^\\d{3}-\\d{3,4}-\\d{4}$";
        if (!phoneNumber.matches(phonePattern)) {
            throw new CustomException(ErrorCode.WRONG_PHONE_NUMBER);
        }
    }

    // 로그인 메소드
    public MemberResponse login(HttpServletRequest httpServletRequest, LoginRequest loginRequest) {
        String username = loginRequest.getUsername();
        Optional<Member> getUser = memberRepository.findByUsername(username);

        if (getUser.isPresent()) {
            Member member = getUser.get();
            if (bCryptPasswordEncoder.matches(loginRequest.getPassword(), member.getPassword())) {
                HttpSession session = httpServletRequest.getSession();
                session.setAttribute("memberId", member.getId());
                session.setMaxInactiveInterval(300000); // 세션 유지 시간 300000초

                return new MemberResponse(member);
            }
        }

        throw new CustomException(ErrorCode.LOGIN_FAIL);
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

            // 원래 이미지가 있을 경우, 삭제 후 재업로드 필요
            if (memberImage.getUrl() !=  null) {
                s3ImageService.deleteImageFromS3(memberImage.getUrl());
            }

            String url = s3ImageService.upload(image);
            memberImage.updateMemberImage(url);
            memberImageRepository.save(memberImage);
            member.updateMemberImage(memberImage);
            memberRepository.save(member);
        }

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
                .createDate(LocalDateTime.now())
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
    public List<FollowerResponse> getFollowers(Long memberId, PageRequest pageRequest) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));
        Page<Follow> followPage = followRepository.findByFromMemberId(member.getId(), pageRequest);

        return followPage.getContent().stream()
                .map(follow -> new FollowerResponse(follow))
                .collect(Collectors.toList());
    }

    // 팔로잉 목록 조회
    public List<FollowingResponse> getFollowings(Long memberId, PageRequest pageRequest) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));
        Page<Follow> followPage = followRepository.findByToMemberId(member.getId(), pageRequest);

        return followPage.getContent().stream()
                .map(follow -> new FollowingResponse(follow))
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
