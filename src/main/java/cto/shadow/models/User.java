package cto.shadow.models;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Set;

public record User(
        long id,
        int failedLoginAttempts,
        OffsetDateTime createdAt,
        OffsetDateTime modifiedAt,
        OffsetDateTime deletedAt,
        OffsetDateTime lastLoginAt,
        OffsetDateTime lockUntil,
        LocalDate dateOfBirth,
        String firstName,
        String lastName,
        String email,
        String phone,
        String username,
        String password,
        String createdBy,
        String modifiedBy,
        String deletedBy,
        Set<Role> authorities
) {
    public User {
        if (id < 0) {
            throw new IllegalArgumentException("ID must be a positive number");
        }
        if (dateOfBirth == null) {
            throw new IllegalArgumentException("DateOfBirth cannot be null");
        }
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("First name cannot be null or blank");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("Last name cannot be null or blank");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or blank");
        }
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be null or blank");
        }
    }
}
