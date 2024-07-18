package com.chimaera.wagubook.dto.response;

import lombok.Data;

@Data
public class MemberProfileResponse {
    private String imageUrl; // 프로필 이미지 url
    private String username; // 사용자 id

    public MemberProfileResponse(String imageUrl, String username) {
        this.imageUrl = imageUrl;
        this.username = username;
    }
}
