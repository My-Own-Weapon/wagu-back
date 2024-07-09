package com.chimaera.wagubook.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(builderMethodName = "newBuilder")
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

    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private MemberImage memberImage; // 프로필 이미지

    @OneToMany(mappedBy = "toMember", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Follow> followers = new ArrayList<>();

    @OneToMany(mappedBy = "fromMember", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Follow> followings = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Post> posts = new ArrayList<>();

    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private LiveRoom liveRoom; // 현재 생방송 중인 라이브 룸

    public void updateProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }


    public void updatePassword(String password) {
        this.password = password;
    }

    public void turnLive(boolean onLive) {
        this.onLive = onLive;
    }

    public Member(Long id){
        this.id = id;
    }
}
