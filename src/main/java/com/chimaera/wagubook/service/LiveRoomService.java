package com.chimaera.wagubook.service;

import com.chimaera.wagubook.entity.*;
import com.chimaera.wagubook.repository.chatMessage.ChaMessageRepository;
import com.chimaera.wagubook.repository.liveRoom.LiveRoomRepository;
import com.chimaera.wagubook.repository.member.FollowRepository;
import com.chimaera.wagubook.repository.member.MemberRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LiveRoomService {

    private final LiveRoomRepository liveRoomRepository;
    private final ChaMessageRepository chaMessageRepository;
    private final FollowRepository followRepository;
    private final MemberRepository memberRepository;
    private final JPAQueryFactory queryFactory;

    @Transactional
    public LiveRoom createLiveRoom(Member member, Store store, String title) {
        // 라이브 스트리밍 방 생성
        LiveRoom liveRoom = LiveRoom.newBuilder()
                .member(member)
                .store(store)
                .title(title)
                .startTime(LocalDateTime.now())
                .build();

        member.turnLive(true);
        memberRepository.save(member);

        liveRoomRepository.save(liveRoom);

        // 팔로워에게 알림 전송
        notifyFollowers(member);

        return liveRoom;
    }

    private void notifyFollowers(Member member) {
        for (Follow follow : member.getFollowers()) {
            Member follower = follow.getFromMember();
            // 팔로워에게 알림 전송 로직
            // 예: 이메일, 앱 푸시 알림 등
        }
    }

    @Transactional
    public void enterLiveRoom(Long liveRoomId, Member member) {
        // 라이브 룸 입장
        LiveRoom liveRoom = liveRoomRepository.findById(liveRoomId).orElseThrow(() -> new IllegalArgumentException("Invalid live room ID"));
        // 참여자 추가 로직 (생략)
    }

    @Transactional
    public void exitLiveRoom(Long liveRoomId, Member member) {
        // 라이브 룸 퇴장
        LiveRoom liveRoom = liveRoomRepository.findById(liveRoomId).orElseThrow(() -> new IllegalArgumentException("Invalid live room ID"));
        // 참여자 제거 로직 (생략)
    }

    @Transactional
    public void sendMessage(Long liveRoomId, Member member, String message) {
        // 채팅 메시지 전송
        LiveRoom liveRoom = liveRoomRepository.findById(liveRoomId).orElseThrow(() -> new IllegalArgumentException("Invalid live room ID"));
        ChatMessage chatMessage = ChatMessage.newBuilder()
                .liveRoom(liveRoom)
                .sender(member)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();

        chaMessageRepository.save(chatMessage);
    }

    @Transactional
    public void sendGift(Long liveRoomId, Member member, String gift) {
        // 선물 보내기 로직 (구체적인 구현은 생략)
    }

    @Transactional
    public void follow(Member fromMember, Member toMember) {
        Follow follow = Follow.newBuilder()
                .fromMember(fromMember)
                .toMember(toMember)
                .isEach(false)
                .build();
        followRepository.save(follow);
    }

    @Transactional
    public void unfollow(Member fromMember, Member toMember) {
        QFollow qFollow = QFollow.follow;
        Follow follow = queryFactory.selectFrom(qFollow)
                .where(qFollow.fromMember.eq(fromMember)
                        .and(qFollow.toMember.eq(toMember)))
                .fetchOne();

        if (follow != null) {
            followRepository.delete(follow);
        }
    }
}
