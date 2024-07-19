package com.chimaera.wagubook.dto.response;

import com.chimaera.wagubook.entity.Follow;
import com.chimaera.wagubook.entity.Member;
import lombok.Data;

@Data
public class FollowingResponse {
    private Long memberId;
    private String username;
    private boolean isEach;
    private String memberImageUrl;
    private boolean isLive;

    public FollowingResponse(Follow follow) {
        Member member = follow.getFromMember();
        String u = member.getMemberImage().getUrl();

        this.memberId = member.getId();
        this.username = member.getUsername();
        this.memberImageUrl = u;
        this.isEach = follow.isEach();
        this.isLive = member.isLive();
    }
}
