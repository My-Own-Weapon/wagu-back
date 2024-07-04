package com.chimaera.wagubook.repository.chatMessage;

import com.chimaera.wagubook.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChaMessageRepository extends JpaRepository<ChatMessage, Long> {
}
