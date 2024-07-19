package com.chimaera.wagubook.repository.liveRoom;

import com.chimaera.wagubook.entity.LiveRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LiveRoomRepository extends JpaRepository<LiveRoom, Long> {
    LiveRoom findByMemberId(Long memberId);
    void deleteBySessionId(String sessionId);
}
