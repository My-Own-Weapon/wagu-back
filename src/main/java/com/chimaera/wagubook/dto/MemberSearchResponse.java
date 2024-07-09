package com.chimaera.wagubook.dto;

import com.chimaera.wagubook.entity.Member;
import com.chimaera.wagubook.entity.MemberImage;
import lombok.Data;

@Data
public class MemberSearchResponse {
    private Long memberId;
    private String memberUsername;
    private MemberImage memberImage;

    public MemberSearchResponse(Member member) {
        this.memberId = member.getId();
        this.memberUsername = member.getUsername();

        if (member.getMemberImage() != null) {
            this.memberImage = member.getMemberImage();
        }
    }
}
