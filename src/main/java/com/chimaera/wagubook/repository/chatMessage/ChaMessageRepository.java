package com.chimaera.wagubook.repository.chatMessage;

import com.chimaera.wagubook.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChaMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findAllByLiveRoomId(Long liveRoomId);
}
