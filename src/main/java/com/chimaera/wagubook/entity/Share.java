package com.chimaera.wagubook.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Share {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String url; // 공유 url
    private LocalDateTime localDateTime; // 공유 시간

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member; // 공유한 멤버
}
