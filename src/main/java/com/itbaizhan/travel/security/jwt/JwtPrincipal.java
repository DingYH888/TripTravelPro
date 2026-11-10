package com.itbaizhan.travel.security.jwt;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtPrincipal implements Serializable {
    private Integer uid;
    private String username;
    private String typ;
    private String jti;
    private List<String> permissions = new ArrayList<>();
}
