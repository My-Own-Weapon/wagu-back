package com.chimaera.wagubook.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder(builderMethodName = "newBuilder")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@ToString(of = {"id", "postMainMenu"})
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "post_id")
    @NotNull
    private Long id;

    private String postMainMenu; // 메인메뉴

    @NotNull
    private boolean isAuto; // AI 자동 생성 기능

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    @NotNull
    private Member member; // 작성자

    @NotNull
    private LocalDateTime createDate; // 작성일
    private LocalDateTime updateDate; // 수정일

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    @NotNull
    private Store store; // 가게

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    private List<Menu> menus = new ArrayList<>(); // posts의 메뉴

    @NotNull
    private Category category;

    @NotNull
    private Permission permission; // post 공개 범위 설정

    public void updatePost(Store store, List<Menu> menus, String postMainMenu, Category category, Permission permission, boolean isAuto) {
        this.postMainMenu = postMainMenu;
        this.store = store;
        this.menus = menus;
        this.category = category;
        this.permission = permission;
        this.isAuto = isAuto;
        this.updateDate = LocalDateTime.now();
    }
}
