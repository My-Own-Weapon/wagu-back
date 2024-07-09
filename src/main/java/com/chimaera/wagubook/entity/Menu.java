package com.chimaera.wagubook.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder(builderMethodName = "newBuilder")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class Menu {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "menu_id")
    private Long id;

    private String menuName; // 메뉴 이름
    private int menuPrice; // 메뉴 가격

    @OneToOne(mappedBy = "menu", cascade = CascadeType.ALL, orphanRemoval = true)
    @NotNull
    private MenuImage menuImage; // 이미지

    private String menuContent; // 메뉴 리뷰

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store; // 가게

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post; // 게시글

    public void setPost(Post post) {
        this.post = post;
    }

    public void setMenuImage(MenuImage menuImage) {
        this.menuImage = menuImage;
    }

    public void updateMenu(String menuName, int menuPrice, String menuContent) {
        this.menuName = menuName;
        this.menuPrice = menuPrice;
        this.menuContent = menuContent;
    }
}
