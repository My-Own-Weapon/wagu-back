package com.chimaera.wagubook.dto;

import com.chimaera.wagubook.entity.Share;
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
