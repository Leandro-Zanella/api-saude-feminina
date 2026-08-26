# Arquitetura

Raiz do código: `api.saude.feminina`

```
api/saude/feminina/
├── AuthApplication.java
├── config/
│   ├── exception/
│   └── security/
├── controller/user/
├── service/
│   ├── user/
│   └── authorization/
├── repository/user/
├── model/user/
├── dto/user/
└── message/
```

Cada domínio ocupa um subpacote dentro de `controller`, `service`, `repository`, `model` e `dto`. Hoje existe apenas `user`.

Fluxo de uma requisição: `controller` → `service` → `repository`.

---

## Raiz

| Classe | Responsabilidade |
|---|---|
| `AuthApplication` | Classe `@SpringBootApplication`. Ponto de entrada da aplicação |

## `config.security`

Configuração de segurança: cadeia de filtros, autenticação por token, CORS e respostas de acesso negado.

| Classe | Responsabilidade |
|---|---|
| `SecurityConfig` | Define o `SecurityFilterChain`: CSRF desabilitado, sessão `STATELESS`, regras de autorização por rota e role, registro do `SecurityFilter`. Expõe os beans `AuthenticationManager` (`ProviderManager` com `DaoAuthenticationProvider`) e `PasswordEncoder` (BCrypt) |
| `SecurityFilter` | `OncePerRequestFilter` executado antes do `UsernamePasswordAuthenticationFilter`. Extrai o token do header `Authorization`, valida, carrega o usuário e popula o `SecurityContextHolder`. Token ausente ou inválido não interrompe a cadeia |
| `TokenService` | Geração e validação de JWT com HMAC-256. Define issuer, subject (e-mail) e expiração |
| `CorsConfig` | Bean `CorsConfigurationSource` com as origens, métodos e headers permitidos |
| `RestAuthenticationEntryPoint` | `AuthenticationEntryPoint`. Escreve 401 em JSON quando não há autenticação |
| `RestAccessDeniedHandler` | `AccessDeniedHandler`. Escreve 403 em JSON quando falta permissão. Escreve direto na resposta em vez de usar `sendError`, evitando o encaminhamento para `/error` |

## `config.exception`

Tratamento centralizado de exceções da camada web.

| Classe | Responsabilidade |
|---|---|
| `GlobalExceptionHandler` | `@RestControllerAdvice`. Converte `MethodArgumentNotValidException` em 400 com mapa `campo → mensagem` e `AuthenticationException` em 401 |

## `controller.user`

Camada HTTP. Recebe DTOs, dispara validação, define status e monta a resposta. Não contém regra de negócio.

| Classe | Responsabilidade |
|---|---|
| `UserController` | Rotas `POST /api/user/register`, `POST /api/user/login` e `GET /api/user` |

## `service.user`

Regras de negócio e delimitação de transações.

| Classe | Responsabilidade |
|---|---|
| `UserService` | Operações de usuário: salvar, listar, buscar por id, buscar por e-mail, verificar existência. `@Transactional(readOnly = true)` na classe, sobrescrito nos métodos de escrita |

## `service.authorization`

Integração entre o domínio e o Spring Security.

| Classe | Responsabilidade |
|---|---|
| `Authorization` | Implementa `UserDetailsService`. Carrega o usuário pelo e-mail para o `DaoAuthenticationProvider`, lançando `UsernameNotFoundException` quando não encontra |

## `repository.user`

Acesso a dados. Interfaces `JpaRepository`, com implementação gerada pelo Spring Data.

| Classe | Responsabilidade |
|---|---|
| `UserRepository` | `JpaRepository<UserModel, Long>`. Consultas derivadas `existsByEmail` e `findByEmail` |

## `model.user`

Entidades JPA e tipos do domínio.

| Classe | Responsabilidade |
|---|---|
| `UserModel` | Entidade da tabela `TB_USER`. Implementa `UserDetails`, convertendo a role em authorities do Spring Security |
| `UserRole` | Enum de papéis: `ADMIN` e `USER` |

## `dto.user`

Contratos de entrada e saída da API, como records. Isola a entidade do corpo das requisições e respostas.

| Classe | Responsabilidade |
|---|---|
| `UserDto` | Entrada do registro, com as anotações de validação |
| `LoginDto` | Entrada do login |
| `LoginResponseDto` | Saída do login: o token |
| `UserResponseDto` | Saída de usuário. Método `from()` converte a entidade, expondo id, nome, e-mail, role e data de criação |

## `message`

| Classe | Responsabilidade |
|---|---|
| `CustomMessage` | Record de resposta genérica no formato `{"message": "..."}` |
