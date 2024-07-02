package com.chimaera.wagubook.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "member_id")
    private Long id;

    private String username; // 사용자 아이디
    private String password;
    private boolean onLive; // 생방송 중인지 여부
    private String name; // 사용자 이름
    private String phoneNumber; // 사용자 전화번호
    private String profileImage; // 프로필 이미지

    @OneToMany(mappedBy = "member")
    @Builder.Default
    private List<Post> posts = new ArrayList<>();
}