package com.chimaera.wagubook.repository.liveRoom;

import com.chimaera.wagubook.entity.LiveRoom;
import com.chimaera.wagubook.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LiveRoomRepository extends JpaRepository<LiveRoom, Long> {
    LiveRoom findByMemberId(Long memberId);
    void deleteBySessionId(String sessionId);
    List<LiveRoom> findByStoreId(Long storeId);
}
