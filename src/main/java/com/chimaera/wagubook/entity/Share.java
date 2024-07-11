package com.chimaera.wagubook.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(builderMethodName = "newBuilder")
public class Share {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "share_id")
    private Long id;
    private String url; // 공유 url
    private LocalDateTime localDateTime; // 공유 시간

    @OneToMany
    private List<Member> memberList;
}
