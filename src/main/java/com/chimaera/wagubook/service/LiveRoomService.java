package com.chimaera.wagubook.service;

import com.chimaera.wagubook.entity.*;
import com.chimaera.wagubook.repository.chatMessage.ChaMessageRepository;
import com.chimaera.wagubook.repository.liveRoom.LiveRoomRepository;
import com.chimaera.wagubook.repository.member.FollowRepository;
import com.chimaera.wagubook.repository.member.MemberRepository;
import com.chimaera.wagubook.repository.store.StoreRepository;
import com.querydsl.jpa.JPAExpressions;
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
    private final StoreRepository storeRepository;
    private final JPAQueryFactory queryFactory;


    @Transactional
    public LiveRoom createLiveRoom(Member member, Store store, String title) {
        // Store 객체가 존재하고, 아직 데이터베이스에 저장되지 않은 경우 저장
        if (store != null && store.getId() == null) {
            store = storeRepository.save(store);
        }

        LiveRoom liveRoom = LiveRoom.newBuilder()
                .member(member)
                .store(store)
                .title(title)
                .startTime(LocalDateTime.now())
                .build();

        member.turnLive(true);
        memberRepository.save(member);
        liveRoomRepository.save(liveRoom);
        notifyFollowers(member);
        return liveRoom;
    }

    private void notifyFollowers(Member member) {
        for (Follow follow : member.getFollowers()) {
            Member follower = follow.getFromMember();
            // 팔로워에게 알림 전송 로직
        }
    }

    public List<LiveRoom> getFollowedLiveRooms(Member member) {
        // 팔로우한 사용자의 라이브 룸 가져오기
        return queryFactory.selectFrom(QLiveRoom.liveRoom)
                .where(QLiveRoom.liveRoom.member.in(
                        JPAExpressions.select(QFollow.follow.toMember)
                                .from(QFollow.follow)
                                .where(QFollow.follow.fromMember.eq(member))
                ))
                .fetch();
    }

    public List<LiveRoom> getStoreLiveRooms(String storeName) {
        return queryFactory.selectFrom(QLiveRoom.liveRoom)
                .where(QLiveRoom.liveRoom.store.storeName.eq(storeName))
                .fetch();
    }

    public LiveRoom getLiveRoomDetails(Long roomId) {
        return liveRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 룸 ID 입니다."));
    }

    @Transactional
    public void enterLiveRoom(Long liveRoomId, Member member) {
        LiveRoom liveRoom = liveRoomRepository.findById(liveRoomId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 룸 ID 입니다."));
        // 참여자 추가 로직
    }

    @Transactional
    public void exitLiveRoom(Long liveRoomId, Member member) {
        LiveRoom liveRoom = liveRoomRepository.findById(liveRoomId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 룸 ID 입니다."));
        // 참여자 제거 로직
    }

    @Transactional
    public void captureLiveRoom(Long liveRoomId, Member member) {
        LiveRoom liveRoom = liveRoomRepository.findById(liveRoomId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 룸 ID 입니다."));
        // 캡쳐 로직
    }

    @Transactional
    public void endLiveRoom(Long liveRoomId, Member member) {
        LiveRoom liveRoom = liveRoomRepository.findById(liveRoomId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 룸 ID 입니다."));
        if (liveRoom.getMember().equals(member)) {
            liveRoomRepository.delete(liveRoom);
            member.turnLive(false);
            memberRepository.save(member);
        } else {
            throw new IllegalArgumentException("방장만 방을 종료할 수 있습니다.");
        }
    }

    @Transactional
    public void sendMessage(Long liveRoomId, Member member, String message) {
        LiveRoom liveRoom = liveRoomRepository.findById(liveRoomId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 룸 ID 입니다."));
        ChatMessage chatMessage = ChatMessage.newBuilder()
                .liveRoom(liveRoom)
                .sender(member)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
        chaMessageRepository.save(chatMessage);
    }

    public List<ChatMessage> getMessages(Long liveRoomId) {
        return chaMessageRepository.findAllByLiveRoomId(liveRoomId);
    }
}