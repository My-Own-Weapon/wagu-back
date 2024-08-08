package com.chimaera.wagubook.dto.request;

import lombok.Data;

@Data
public class WebSocketRequest {
    private String senderId;
    private String senderName;
    private String type;
    private String data;
    private String roomURL;
}
