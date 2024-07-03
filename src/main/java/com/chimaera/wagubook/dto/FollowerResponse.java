package com.chimaera.wagubook.dto;

import com.chimaera.wagubook.entity.Follow;
import lombok.Data;

@Data
public class FollowerResponse {
    private Long memberId;
    private String username;
    private boolean isEach;

    public FollowerResponse(Follow follow) {
        this.memberId = follow.getToMember().getId();
        this.username = follow.getToMember().getUsername();
        this.isEach = follow.isEach();
    }
}
