package com.chimaera.wagubook.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class ShareResponse {
    private String url;
    private List<StoreResponse> storeResponses;
    private List<FriendResponse> friendResponses;

    public ShareResponse(){
    }
}
