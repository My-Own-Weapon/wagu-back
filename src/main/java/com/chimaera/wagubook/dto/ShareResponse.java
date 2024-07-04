package com.chimaera.wagubook.dto;

import com.chimaera.wagubook.entity.Share;
import lombok.Data;

@Data
public class ShareResponse {
    private String url;

    public ShareResponse(Share share){
        this.url = share.getUrl();
    }
}
