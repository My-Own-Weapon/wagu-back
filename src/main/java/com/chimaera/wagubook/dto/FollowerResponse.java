package com.chimaera.wagubook.dto;

import com.chimaera.wagubook.entity.Follow;
import com.chimaera.wagubook.entity.Member;
import jakarta.validation.constraints.Null;
import lombok.Data;
import org.eclipse.jdt.internal.compiler.ast.NullLiteral;

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
        this.memberImageUrl = (u==null) ? null : u;
        this.isEach = follow.isEach();
    }
}
