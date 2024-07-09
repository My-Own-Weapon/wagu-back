package com.chimaera.wagubook.dto;

import com.chimaera.wagubook.entity.Member;
import com.chimaera.wagubook.entity.MemberImage;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class FriendResponse {
    private String name;
    private MemberImage memberImage;
    private String posx;
    private String posy;

    public FriendResponse(Member member, double x, double y){
        this.posx = ""+x;
        this.posy = ""+y;
        this.name = member.getName();
        this.memberImage = member.getMemberImage();
    }
}

