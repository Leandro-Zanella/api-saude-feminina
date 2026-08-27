package api.saude.feminina.dto.user;

/** Saída do login: o usuário autenticado e o token. */
public record LoginResponseDto(UserResponseDto user, String token) {
}
