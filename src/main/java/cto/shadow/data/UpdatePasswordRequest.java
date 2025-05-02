package cto.shadow.data;

import com.alibaba.fastjson2.annotation.JSONCompiled;

@JSONCompiled
public record UpdatePasswordRequest(
        String oldPassword,
        String newPassword
) {
    public UpdatePasswordRequest {
        if (oldPassword == null || oldPassword.isBlank()) {
            throw new IllegalArgumentException("Old password cannot be null or blank");
        }
        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("New password cannot be null or blank");
        }
    }
}
