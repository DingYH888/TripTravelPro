package com.itbaizhan.travel.api.dto;

import lombok.Data;

import java.util.List;

@Data
public class TokenResponse {
    private String tokenType = "Bearer";
    private String accessToken;
    private String refreshToken;
    private long expiresIn;
    private String username;
    private List<String> permissions;
}
