package cto.shadow.dtos;

import com.alibaba.fastjson2.annotation.JSONCompiled;

@JSONCompiled
public record UserLogin(
        String username,
        String password
) {
    public UserLogin {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be null or blank");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be null or blank");
        }
    }
}
