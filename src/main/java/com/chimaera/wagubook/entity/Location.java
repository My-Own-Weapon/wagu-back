package com.chimaera.wagubook.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter @Setter
public class Location {
    private String address; // 주소
    private double posx; // x좌표
    private double posy; // y좌표

}
