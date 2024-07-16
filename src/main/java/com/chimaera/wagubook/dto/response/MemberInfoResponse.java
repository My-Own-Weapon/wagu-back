package com.chimaera.wagubook.dto.response;

import lombok.Data;

@Data
public class MemberInfoResponse {
    private int followerNum;
    private int followingNum;
    private int postNum;

    public MemberInfoResponse(int followerNum, int followingNum, int postNum) {
        this.followerNum = followerNum;
        this.followingNum = followingNum;
        this.postNum = postNum;
    }
}
