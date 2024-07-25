package com.chimaera.wagubook.dto.response;

import lombok.Data;

@Data
public class PostUpdateCheckResponse {
    private boolean isUpdate;

    public PostUpdateCheckResponse(boolean isUpdate) {
        this.isUpdate = isUpdate;
    }
}
