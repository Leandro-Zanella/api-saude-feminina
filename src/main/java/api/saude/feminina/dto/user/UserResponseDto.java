package api.saude.feminina.dto.user;

import api.saude.feminina.model.user.UserModel;
import api.saude.feminina.model.user.UserRole;

import java.time.LocalDateTime;

public record UserResponseDto(
        Long id,
        String name,
        String email,
        UserRole role,
        LocalDateTime createdAt
) {

    public static UserResponseDto from(UserModel user) {
        return new UserResponseDto(user.getId(), user.getName(), user.getEmail(), user.getRole(), user.getCreatedAt());
    }
}
