package cto.shadow.data;

import com.alibaba.fastjson2.annotation.JSONCompiled;
import org.eclipse.collections.api.set.ImmutableSet;

import java.time.OffsetDateTime;

@JSONCompiled
public record User(
        long id,
        OffsetDateTime lastLoginAt,
        String username,
        ImmutableSet<Role> authorities,
        ImmutableSet<User> followers,
        ImmutableSet<User> following
) {
    public User {
        if (id < 0) {
            throw new IllegalArgumentException("ID must be a positive number");
        }
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be null or blank");
        }
    }
}
