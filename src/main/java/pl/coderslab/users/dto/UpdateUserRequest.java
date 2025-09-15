package pl.coderslab.users.dto;

import jakarta.persistence.Entity;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.*;
import jakarta.validation.Valid;
import java.util.Set;


public record UpdateUserRequest (
        @Size(max = 50, message = "First name too long")
        String firstName,

        @Size(max = 50, message = "Last name too long")
        String lastName,

        @Email(message = "Invalid email format")
        String email,

        Set<Long> roleIds
) {}
