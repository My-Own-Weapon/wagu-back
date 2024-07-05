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
    private Long id;
    private String url; // 공유 url
    private LocalDateTime localDateTime; // 공유 시간

    @OneToMany
    @JoinColumn(name = "member_id")
    private List<Member> memberList;

    @OneToMany
    @JoinColumn(name = "store_id")
    private List<Store> storeList;
}
