package cto.shadow.dtos;

import com.alibaba.fastjson2.annotation.JSONCompiled;

import java.sql.Date;

@JSONCompiled
public record UserRegister(
        Date dateOfBirth,
        String firstName,
        String lastName,
        String email,
        String phone,
        String username,
        String password
) {
    public UserRegister {
        if (dateOfBirth == null) {
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
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be null or blank");
        }
    }
}
