package com.chimaera.wagubook.dto;

import com.chimaera.wagubook.entity.Member;
import com.chimaera.wagubook.entity.Store;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class FriendResponse {
    private String name;
    private String profileImage;
    private String posx;
    private String posy;

    public FriendResponse(Member member, double x, double y){
        this.posx = ""+x;
        this.posy = ""+y;
        this.name = member.getName();
        this.profileImage = member.getProfileImage();
    }
}

