package com.chimaera.wagubook.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(builderMethodName = "newBuilder")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "chat_message_id")
    private Long id;

    private String message; // 채팅 메시지 내용

    private LocalDateTime timestamp; // 메시지 전송 시간

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "live_room_id")
    private LiveRoom liveRoom; // 채팅 메시지가 속한 라이브 룸

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member sender; // 메시지 보낸 사람
}
