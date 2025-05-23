package cto.shadow.data;

import com.alibaba.fastjson2.annotation.JSONCompiled;

@JSONCompiled
public record Role(
        long id,
        String authority
) {
    public Role {
        if (id < 0) {
            throw new IllegalArgumentException("ID must be a positive number");
        }
        if (authority == null || authority.isBlank()) {
            throw new IllegalArgumentException("Authority cannot be null or blank");
        }
    }
}
