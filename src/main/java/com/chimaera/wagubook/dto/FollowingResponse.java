package com.chimaera.wagubook.dto;

import com.chimaera.wagubook.entity.Follow;
import com.chimaera.wagubook.entity.Member;
import lombok.Data;

@Data
public class FollowingResponse {
    private Long memberId;
    private String username;
    private boolean isEach;
    private String memberImageUrl;
    private boolean isOnLive;

    public FollowingResponse(Follow follow) {
        Member member = follow.getFromMember();
        String u = member.getMemberImage().getUrl();

        this.memberId = member.getId();
        this.username = member.getUsername();
        this.memberImageUrl = (u==null) ? null : u;
        this.isEach = follow.isEach();
        this.isOnLive = member.isOnLive();
    }
}
