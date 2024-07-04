package com.chimaera.wagubook.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

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
    private boolean isValid;
}
