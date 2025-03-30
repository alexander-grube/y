package cto.shadow.models;

import java.util.Set;

public record User(
        long id,
        long createdAt,
        long modifiedAt,
        long deletedAt,
        long dateOfBirth,
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
        if (dateOfBirth < 0) {
            throw new IllegalArgumentException("DateOfBirth must be a positive number");
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
