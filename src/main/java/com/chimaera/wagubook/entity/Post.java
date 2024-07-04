package com.chimaera.wagubook.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder(builderMethodName = "newBuilder")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@ToString(of = {"id", "postMainMenu", "postContent"})
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "post_id")
    private Long id;

    private String postMainMenu; // 메인메뉴
    private String postImage; // 이미지
    private String postContent; // 내용
    private boolean isAuto; // AI 자동 생성 기능

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    @JsonIgnore
    private Member member; // 작성자

    private LocalDateTime createDate; // 작성일
    private LocalDateTime updateDate; // 수정일

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    @JsonIgnore
    private Store store; // 가게

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Menu> menus = new ArrayList<>(); // posts의 메뉴

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @JsonIgnore
    private Category category;

    private Permission permission; // post 공개 범위 설정

}
