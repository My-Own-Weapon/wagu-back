package com.chimaera.wagubook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
public class WagubookApplication {

    public static void main(String[] args) {
        SpringApplication.run(WagubookApplication.class, args);
    }

}