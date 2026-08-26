package api.saude.feminina.dto.user;

import jakarta.validation.constraints.NotBlank;

public record LoginDto(

        @NotBlank
        String email,

        @NotBlank
        String password
) {
}
