package com.chimaera.wagubook.dto;

import com.chimaera.wagubook.entity.Follow;
import lombok.Data;

@Data
public class FollowingResponse {
    private Long memberId;
    private String username;
    private boolean isEach;

    public FollowingResponse(Follow follow) {
        this.memberId = follow.getFromMember().getId();
        this.username = follow.getFromMember().getUsername();
        this.isEach = follow.isEach();
    }
}
