package pl.coderslab.users.dto;

import jakarta.validation.constraints.*;
import pl.coderslab.roles.Role;
import pl.coderslab.users.User;

import java.util.Set;
import java.util.stream.Collectors;

public record EditUserRequest(
        @NotNull(message = "User ID is required")
        Long id,

        @NotBlank(message = "First name is required")
        @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
        String lastName,

        @NotBlank(message = "Email is required")
        @Email(message = "Please provide a valid email address")
        String email,

        @NotEmpty(message = "At least one role must be selected")
        Set<Long> roleIds
) {
    // Factory method to create from User entity
    public static EditUserRequest fromUser(User user) {
        return new EditUserRequest(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRoles().stream()
                        .map(Role::getId)
                        .collect(Collectors.toSet())
        );
    }
}
