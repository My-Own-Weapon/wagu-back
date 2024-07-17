package com.chimaera.wagubook.dto.response;

import com.chimaera.wagubook.entity.Member;
import com.chimaera.wagubook.entity.MemberImage;
import lombok.Data;

@Data
public class MemberSearchResponse {
    private Long memberId;
    private String memberUsername;
    private MemberImage memberImage;
    private boolean to; // 내가 팔로우하는 사람의 상태
    private boolean from; // 나를 팔로우하는 사람의 상태
    private boolean isEach; // 서로 팔로우하는 상태

    // currentUser: 현재 로그인하고 있는 유저
    public MemberSearchResponse(Member member, Member currentUser) {
        this.memberId = member.getId();
        this.memberUsername = member.getUsername();

        this.to = currentUser.getFollowings().stream()
                .anyMatch(follow -> follow.getToMember().getId().equals(member.getId()));

        this.from = currentUser.getFollowers().stream()
                .anyMatch(follow -> follow.getFromMember().getId().equals(member.getId()));

        this.isEach = this.to && this.from;

        if (member.getMemberImage() != null) {
            this.memberImage = member.getMemberImage();
        }

    }
}
