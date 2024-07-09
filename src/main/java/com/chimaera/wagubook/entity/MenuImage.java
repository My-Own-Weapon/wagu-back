package com.chimaera.wagubook.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class MenuImage {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "menu_image_id")
    private Long id;

    private String url;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id")
    @JsonIgnore
    private Menu menu; // 메뉴

    public void updateMenuImage(String url) {
        this.url = url;
    }
}
