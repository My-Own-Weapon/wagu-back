package com.chimaera.wagubook.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MemberRequest {
    @NotNull
    private String username;
    @NotNull
    private String password;
    @NotNull
    private String passwordConfirm;
    @NotNull(message = "이름은 한글이어야 합니다.")
    private String name;
    @NotNull(message = "전화번호는 -가 포함된 ")
    private String phoneNumber;
}
