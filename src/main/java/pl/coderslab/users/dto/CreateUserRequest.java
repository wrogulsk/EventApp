package pl.coderslab.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record CreateUserRequest(
        @NotBlank(message = "First name is required")
        @Size(max = 50, message = "First name too long")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 50, message = "Last name too long")
        String lastName,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String password,

        @NotEmpty(message = "At least one role is required")
        Set<Long> roleIds
) {}
