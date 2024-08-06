package com.chimaera.wagubook.dto.request;

import lombok.Data;

@Data
public class WebSocketRequest {
    private String senderId;
    private String type;
    private String data;
    private String roomURL;
    private String username;
    private String receiver;
    private Object offer;
    private Object answer;
    private Object candidate;
    private Object sdp;
}
