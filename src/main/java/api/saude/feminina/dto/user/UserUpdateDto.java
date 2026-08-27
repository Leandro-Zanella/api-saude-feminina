package api.saude.feminina.dto.user;

import jakarta.validation.constraints.NotBlank;

public record UserUpdateDto(

        @NotBlank
        String name,

        @NotBlank
        String email,

        @NotBlank
        String userRole
) {
}
