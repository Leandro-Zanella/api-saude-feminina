package api.saude.feminina.config.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import api.saude.feminina.model.user.UserModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class TokenService {

    private static final String TOKEN_ISSUER = "api-saude-feminina";

    private final Algorithm algorithm;
    private final Duration expiration;

    public TokenService(@Value("${api.security.token.secret}") String secret,
                        @Value("${api.security.token.expiration:PT2H}") Duration expiration) {
        this.algorithm = Algorithm.HMAC256(secret);
        this.expiration = expiration;
    }

    public String generateToken(UserModel user) {
        try {
            return JWT.create()
                    .withIssuer(TOKEN_ISSUER)
                    .withSubject(user.getEmail())
                    .withIssuedAt(Instant.now())
                    .withExpiresAt(this.genExpirationDate())
                    .sign(algorithm);
        } catch (JWTCreationException err) {
            throw new IllegalStateException("Erro ao gerar o token de autenticação", err);
        }
    }

    /** Retorna o e-mail (subject) usado para gerar o token. Lança JWTVerificationException se inválido. */
    public String validateToken(String token) {
        return JWT.require(algorithm)
                .withIssuer(TOKEN_ISSUER)
                .build()
                .verify(token)
                .getSubject();
    }

    private Instant genExpirationDate() {
        return Instant.now().plus(expiration);
    }

}
