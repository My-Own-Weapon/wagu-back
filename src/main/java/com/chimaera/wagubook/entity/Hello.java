package com.chimaera.wagubook.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class Hello {
    @GeneratedValue @Id
    private Long id;
}
