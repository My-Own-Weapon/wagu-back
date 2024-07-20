package com.chimaera.wagubook.dto.response;

import com.chimaera.wagubook.entity.LiveRoom;
import lombok.Data;

@Data
public class LiveResponse {
    String profileImage;    //라이브 중인 사람의 프로필 사진
    String sessionId;       //라이브 세션 ID
    String userName;        //라이브 중인 사람의 닉네임
    String address;
    String storeName;


    public LiveResponse(LiveRoom liveRoom){
        this.profileImage=liveRoom.getMember().getMemberImage().getUrl();
        this.sessionId=liveRoom.getSessionId();
        this.userName=liveRoom.getMember().getName();
        this.storeName=liveRoom.getStore().getStoreName();
        this.address=liveRoom.getStore().getStoreLocation().getAddress();
    }
}
