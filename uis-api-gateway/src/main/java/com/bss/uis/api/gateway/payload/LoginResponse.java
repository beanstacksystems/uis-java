package com.bss.uis.api.gateway.payload;

import lombok.Data;

@Data
public class LoginResponse {
    public LoginResponse(String token) {
        this.token = token;
    }

    private String token;
}
