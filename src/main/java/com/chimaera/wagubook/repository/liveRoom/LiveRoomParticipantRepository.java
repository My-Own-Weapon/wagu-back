package com.chimaera.wagubook.repository.liveRoom;

import com.chimaera.wagubook.entity.LiveRoomParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LiveRoomParticipantRepository extends JpaRepository<LiveRoomParticipant, Long> {
    void deleteBySessionId(String sessionId);
    LiveRoomParticipant findByMemberId(Long memberId);
    void deleteByMemberId(Long memberId);
    List<LiveRoomParticipant> findBySessionId(String sessionId);
}
