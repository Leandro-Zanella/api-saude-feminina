package api.saude.feminina.dto.user;

import api.saude.feminina.model.user.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserDto(

        @NotBlank
        String name,

        @NotBlank
        String email,

        @NotBlank
        String password,

        @NotNull
        UserRole userRole
) {
}
