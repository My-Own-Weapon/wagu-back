package com.chimaera.wagubook.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketResponse {
    private String senderId;    // sender 로그인 아이디
    private String senderName;  // sender 이름
    private String type;        // [""]
    private String data;        // 메시지
    private String roomURL;     // 방 url
    private List<String> allUsersNickNames; // 방에 참여하고 있는 사람들 리스트(처음 입장 시)
}
