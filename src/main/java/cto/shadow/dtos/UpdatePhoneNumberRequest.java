package cto.shadow.dtos;

import com.alibaba.fastjson2.annotation.JSONCompiled;

@JSONCompiled
public record UpdatePhoneNumberRequest(
        String phone
) {
    public UpdatePhoneNumberRequest {
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("Phone number cannot be null or blank");
        }
    }
}
