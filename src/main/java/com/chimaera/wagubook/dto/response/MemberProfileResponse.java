package com.chimaera.wagubook.dto.response;

import lombok.Data;

@Data
public class MemberProfileResponse {
    private Long memberId;  // 사용자의 진짜 id
    private String imageUrl; // 프로필 이미지 url
    private String username; // 사용자 별명
    private String name;     // 사용자 아룸

    public MemberProfileResponse( Long memberId, String imageUrl, String username, String name) {
        this.memberId = memberId;
        this.imageUrl = imageUrl;
        this.username = username;
        this.name = name;
    }
}
