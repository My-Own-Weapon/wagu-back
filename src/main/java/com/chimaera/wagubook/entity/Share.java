package com.chimaera.wagubook.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashMap;

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
    @Lob
    private HashMap<Long, Integer> voteStoreList;

}

