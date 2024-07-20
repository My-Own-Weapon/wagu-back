package com.chimaera.wagubook.dto.response;

import lombok.Data;

@Data
public class MemberInfoResponse {
    private int followerNum;
    private int followingNum;
    private int postNum;
    private String userName;
    private String profileImage;

    public MemberInfoResponse(int followerNum, int followingNum, int postNum, String name, String url) {
        this.followerNum = followerNum;
        this.followingNum = followingNum;
        this.postNum = postNum;
        this.userName = name;
        this.profileImage = url;
    }
}
