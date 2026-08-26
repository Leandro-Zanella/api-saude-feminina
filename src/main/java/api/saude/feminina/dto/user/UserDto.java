package api.saude.feminina.dto.user;

import jakarta.validation.constraints.NotBlank;

public record UserDto(

        @NotBlank
        String name,

        @NotBlank
        String email,

        @NotBlank
        String password,

        @NotBlank
        String userRole
) {
}
