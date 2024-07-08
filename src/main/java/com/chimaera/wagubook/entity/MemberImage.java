package com.chimaera.wagubook.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder(builderMethodName = "newBuilder")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class MemberImage {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "member_image_id")
    private Long id;

    private String url;

    @OneToOne
    private Member member;

    public void updateProfileImage(String url) {
        this.url = url;
    }
}
