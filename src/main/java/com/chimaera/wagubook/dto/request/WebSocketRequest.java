package com.chimaera.wagubook.dto.request;

import lombok.Data;

@Data
public class WebSocketRequest {
    private String senderId;    //유저 아이디
    private String senderName;  //닉네임
    private String type;        //[chat, join_room]
    private String data;        //채팅 내용
    private String roomURL;     //방 url
}
