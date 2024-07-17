package com.chimaera.wagubook.dto.response;

import com.chimaera.wagubook.entity.Follow;
import com.chimaera.wagubook.entity.Member;
import lombok.Data;

@Data
public class FollowerResponse {
    private Long memberId;
    private String username;
    private boolean isEach;
    private String memberImageUrl;

    public FollowerResponse(Follow follow) {
        Member member = follow.getToMember();
        String u = member.getMemberImage().getUrl();

        this.memberId = member.getId();
        this.username = member.getUsername();
        this.memberImageUrl = u;
        this.isEach = follow.isEach();
    }
}
