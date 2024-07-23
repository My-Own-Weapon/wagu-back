package com.chimaera.wagubook.service;

import com.chimaera.wagubook.dto.response.LiveResponse;
import com.chimaera.wagubook.entity.*;
import com.chimaera.wagubook.repository.liveRoom.LiveRoomParticipantRepository;
import com.chimaera.wagubook.repository.liveRoom.LiveRoomRepository;
import com.chimaera.wagubook.repository.member.FollowRepository;
import com.chimaera.wagubook.repository.member.MemberRepository;
import com.chimaera.wagubook.repository.store.StoreRepository;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LiveRoomService {

    private final LiveRoomRepository liveRoomRepository;
    private final MemberRepository memberRepository;
    private final StoreRepository storeRepository;
    private final JPAQueryFactory queryFactory;
    private final LiveRoomParticipantRepository liveRoomParticipantRepository;



    @Transactional
    public LiveRoom createLiveRoom(Member member, Store store, String title) {
        // Store 객체가 존재하고, 아직 데이터베이스에 저장되지 않은 경우 저장
        if (store != null && store.getId() == null) {
            store = storeRepository.save(store);
        }

        LiveRoom liveRoom = LiveRoom.newBuilder()
                .member(member)
                .store(store)
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


    public List<LiveResponse> getFollowedLiveRooms(Long memberId) {
        // 팔로우한 사용자의 라이브 룸 가져오기
        List<LiveRoom> allFollowedRooms = liveRoomRepository.findAllFollowedRooms(memberId);

        if(allFollowedRooms.isEmpty())
            return new ArrayList<>();

        return allFollowedRooms.stream().map(room -> new LiveResponse(room)).collect(Collectors.toList());
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

    // TODO: 수정
    @Transactional
    public void saveLiveRoomParticipant(LiveRoomParticipant liveRoomParticipant) {
        liveRoomParticipantRepository.save(liveRoomParticipant);
    }

    public LiveRoom getLiveRoomBySessionId(String sessionId) {
        return liveRoomRepository.findBySessionId(sessionId);
    }

    // TODO: 수정
    @Transactional
    public void deleteLiveRoom(String sessionId){
        // 먼저 liveRoomParticipant 삭제
        liveRoomParticipantRepository.deleteBySessionId(sessionId);

        // 그 다음 LiveRoom 삭제
        liveRoomRepository.deleteBySessionId(sessionId);
    }

    public LiveRoomParticipant getLiveRoomParticipantByMemberId(Long memberId) {
        return liveRoomParticipantRepository.findByMemberId(memberId);
    }

    @Transactional
    public void deleteLiveRoomParticipant(Long memberId){
        liveRoomParticipantRepository.deleteByMemberId(memberId);
    }

    // sessionId로 LiveRoomParticipant 찾기
    public List<LiveRoomParticipant> getLiveRoomParticipantBySessionId(String sessionId) {
        return liveRoomParticipantRepository.findBySessionId(sessionId);
    }




}