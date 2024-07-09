package com.chimaera.wagubook.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Builder(builderMethodName = "newBuilder")
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class LiveRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "live_room_id")
    private Long id;

    private String title; // 라이브 스트리밍 제목

    private LocalDateTime startTime; // 시작 시간

    private LocalDateTime endTime; // 종료 시간

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store; // 라이브 스트리밍이 진행되는 가게

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member; // 라이브 스트리밍 주최자

    @OneToMany(mappedBy = "liveRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMessage> chatMessages = new ArrayList<>(); // 채팅 메시지들
}
