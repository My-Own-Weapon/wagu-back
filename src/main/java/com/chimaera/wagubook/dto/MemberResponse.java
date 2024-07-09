package com.chimaera.wagubook.dto;

import com.chimaera.wagubook.entity.Member;
import com.chimaera.wagubook.entity.MemberImage;
import lombok.Data;

@Data
public class MemberResponse {
    private String memberUsername;
    private MemberImage memberImage;

    public MemberResponse(Member member) {
        this.memberUsername = member.getUsername();

        if (member.getMemberImage() != null) {
            this.memberImage = member.getMemberImage();
        }
    }
}
