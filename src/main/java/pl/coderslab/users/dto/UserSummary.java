package pl.coderslab.users.dto;

public record UserSummary(
        Long id,
        String firstName,
        String lastName,
        String email
) {}