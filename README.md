# Arquitetura

Raiz do código: `api.saude.feminina`

```
api/saude/feminina/
├── AuthApplication.java
├── config/
│   ├── exception/
│   ├── security/
│   └── web/
├── controller/
│   ├── user/
│   ├── article/
│   └── media/
├── service/
│   ├── user/
│   ├── authorization/
│   ├── article/
│   └── media/
├── repository/
│   ├── user/
│   └── article/
├── model/
│   ├── user/
│   └── article/
├── dto/
│   ├── user/
│   ├── article/
│   └── media/
└── message/
```

Cada domínio ocupa um subpacote dentro de `controller`, `service`, `repository`, `model` e `dto`. Hoje existem `user`, `article` e `media`.

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
| `SecurityConfig` | Define o `SecurityFilterChain`: CSRF desabilitado, sessão `STATELESS`, regras de autorização por rota e role, registro do `SecurityFilter`. Expõe os beans `AuthenticationManager` (`ProviderManager` com `DaoAuthenticationProvider`) e `PasswordEncoder` (BCrypt, estático para não criar ciclo com o `UserService`) |
| `SecurityFilter` | `OncePerRequestFilter` executado antes do `UsernamePasswordAuthenticationFilter`. Extrai o token do header `Authorization`, valida, carrega o usuário e popula o `SecurityContextHolder`. Token ausente ou inválido não interrompe a cadeia |
| `TokenService` | Geração e validação de JWT com HMAC-256. Define issuer, subject (e-mail) e expiração |
| `CorsConfig` | Bean `CorsConfigurationSource` com as origens, métodos e headers permitidos |
| `RestAuthenticationEntryPoint` | `AuthenticationEntryPoint`. Escreve 401 em JSON quando não há autenticação |
| `RestAccessDeniedHandler` | `AccessDeniedHandler`. Escreve 403 em JSON quando falta permissão. Escreve direto na resposta em vez de usar `sendError`, evitando o encaminhamento para `/error` |

## `config.exception`

Tratamento centralizado de exceções da camada web.

| Classe | Responsabilidade |
|---|---|
| `GlobalExceptionHandler` | `@RestControllerAdvice`. Converte `MethodArgumentNotValidException` em 400 com mapa `campo → mensagem`, `NotFoundException` em 404 e `AuthenticationException` em 401 |
| `NotFoundException` | `RuntimeException` de recurso inexistente, lançada pelos services |

## `config.web`

| Classe | Responsabilidade |
|---|---|
| `WebConfig` | `WebMvcConfigurer` que publica o diretório de uploads em `/media/**`, para a web e o app carregarem as mídias sem token |

## `controller.user`

Camada HTTP. Recebe DTOs, dispara validação, define status e monta a resposta. Não contém regra de negócio.

| Classe | Responsabilidade |
|---|---|
| `UserController` | Rotas `POST /api/user/register`, `POST /api/user/login` e `GET /api/user` |

## `controller.article`

| Classe | Responsabilidade |
|---|---|
| `ArticleController` | CRUD em `/api/article`. Escrita restrita a `ADMIN`; leitura para qualquer usuário autenticado. O autor vem do `@AuthenticationPrincipal` |

## `controller.media`

| Classe | Responsabilidade |
|---|---|
| `MediaController` | `POST /api/media`, upload multipart das imagens usadas no editor. Restrito a `ADMIN` |

## `service.user`

Regras de negócio e delimitação de transações.

| Classe | Responsabilidade |
|---|---|
| `UserService` | Operações de usuário: registrar, listar, buscar por e-mail, verificar existência. Faz o encode da senha e resolve o papel, criando-o na primeira vez que é usado. `@Transactional(readOnly = true)` na classe, sobrescrito nos métodos de escrita |

## `service.authorization`

Integração entre o domínio e o Spring Security.

| Classe | Responsabilidade |
|---|---|
| `Authorization` | Implementa `UserDetailsService`. Carrega o usuário pelo e-mail para o `DaoAuthenticationProvider`, lançando `UsernameNotFoundException` quando não encontra |

## `service.article`

| Classe | Responsabilidade |
|---|---|
| `ArticleService` | CRUD de artigos. Lista da mais recente para a mais antiga por `updatedAt`, para o app refletir as edições no topo. Lança `NotFoundException` quando o id não existe |

## `service.media`

| Classe | Responsabilidade |
|---|---|
| `MediaService` | Grava o arquivo em disco com nome `UUID`, preservando a extensão, e devolve a URL de leitura |

## `repository.user`

Acesso a dados. Interfaces `JpaRepository`, com implementação gerada pelo Spring Data.

| Classe | Responsabilidade |
|---|---|
| `UserRepository` | `JpaRepository<UserModel, Long>`. Consultas derivadas `existsByEmail` e `findByEmail` |
| `RoleRepository` | `JpaRepository<RoleModel, Long>`. Consulta derivada `findByName` |

## `repository.article`

| Classe | Responsabilidade |
|---|---|
| `ArticleRepository` | `JpaRepository<ArticleModel, Long>`. Consulta derivada `findAllByOrderByUpdatedAtDesc` |

## `model.user`

Entidades JPA e tipos do domínio.

| Classe | Responsabilidade |
|---|---|
| `UserModel` | Entidade da tabela `TB_USER`. Implementa `UserDetails`, convertendo o papel em authorities do Spring Security. `ADMIN` acumula `ROLE_ADMIN` e `ROLE_USER` |
| `RoleModel` | Entidade da tabela `TB_ROLE`. Isola o papel de acesso, referenciado por `TB_USER.role_id` |

## `model.article`

| Classe | Responsabilidade |
|---|---|
| `ArticleModel` | Entidade da tabela `TB_ARTICLE`. O `contentHtml` é `TEXT` e guarda o HTML do editor rico. `createdAt` e `updatedAt` são preenchidos pelo Hibernate |

## `dto.user`

Contratos de entrada e saída da API, como records. Isola a entidade do corpo das requisições e respostas.

| Classe | Responsabilidade |
|---|---|
| `UserDto` | Entrada do registro, com as anotações de validação. O papel chega como texto: a web envia `ADMIN`, o app envia `USER` |
| `LoginDto` | Entrada do login |
| `LoginResponseDto` | Saída do login: o token |
| `UserResponseDto` | Saída de usuário. Método `from()` converte a entidade, expondo id, nome, e-mail, papel e data de criação |

## `dto.article`

| Classe | Responsabilidade |
|---|---|
| `ArticleDto` | Entrada de criação e edição, com as anotações de validação |
| `ArticleResponseDto` | Saída de artigo. Método `from()` converte a entidade, achatando o autor em `authorName` |

## `dto.media`

| Classe | Responsabilidade |
|---|---|
| `MediaResponseDto` | Saída do upload: a URL do arquivo gravado |

## `message`

| Classe | Responsabilidade |
|---|---|
| `CustomMessage` | Record de resposta genérica no formato `{"message": "..."}` |

---

# Endpoints

| Método | Rota | Acesso |
|---|---|---|
| `POST` | `/api/user/register` | público |
| `POST` | `/api/user/login` | público |
| `GET` | `/api/user` | `ADMIN` |
| `GET` | `/api/article` | autenticado |
| `GET` | `/api/article/{id}` | autenticado |
| `POST` | `/api/article` | `ADMIN` |
| `PUT` | `/api/article/{id}` | `ADMIN` |
| `DELETE` | `/api/article/{id}` | `ADMIN` |
| `POST` | `/api/media` | `ADMIN` |
| `GET` | `/media/{arquivo}` | público |

`GET /media/**` é público porque a tag `<img>` não envia o header `Authorization`. Exigir token nessa rota faria toda imagem quebrar na web e no app. A proteção é o nome ser um `UUID` aleatório, e só `ADMIN` conseguir criar arquivos.

---

# Como rodar

```bash
docker compose up -d
./mvnw spring-boot:run
```

A aplicação sobe em `http://localhost:8080` e o Hibernate cria o schema (`spring.jpa.hibernate.ddl-auto=update`).

---

# Testando com curl

## Cadastro

A web cadastra `ADMIN`, o app cadastra `USER`.

```bash
curl -X POST http://localhost:8080/api/user/register \
  -H 'Content-Type: application/json' \
  -d '{"name":"Leandro Zanella","email":"admin@saudefeminina.com","password":"admin123","userRole":"ADMIN"}'

curl -X POST http://localhost:8080/api/user/register \
  -H 'Content-Type: application/json' \
  -d '{"name":"Maria Souza","email":"maria@saudefeminina.com","password":"maria123","userRole":"USER"}'
```

E-mail repetido responde `409`:

```bash
curl -i -X POST http://localhost:8080/api/user/register \
  -H 'Content-Type: application/json' \
  -d '{"name":"Outro","email":"admin@saudefeminina.com","password":"senha123","userRole":"USER"}'
```

## Login

Guarda o token numa variável para reaproveitar nos comandos seguintes.

```bash
token() {
  curl -s -X POST http://localhost:8080/api/user/login \
    -H 'Content-Type: application/json' \
    -d "{\"email\":\"$1\",\"password\":\"$2\"}" \
    | python3 -c 'import sys,json;print(json.load(sys.stdin)["token"])'
}

ADMIN=$(token admin@saudefeminina.com admin123)
USER=$(token maria@saudefeminina.com maria123)
```

Se preferir, chame o login direto e copie o token da resposta:

```bash
curl -X POST http://localhost:8080/api/user/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@saudefeminina.com","password":"admin123"}'
```

## Listar usuários (só `ADMIN`)

```bash
curl -H "Authorization: Bearer $ADMIN" http://localhost:8080/api/user
```

## Criar artigo

```bash
curl -X POST http://localhost:8080/api/article \
  -H "Authorization: Bearer $ADMIN" \
  -H 'Content-Type: application/json' \
  -d '{
    "title": "Entendendo o ciclo menstrual",
    "summary": "As quatro fases do ciclo e o que acontece no corpo em cada uma delas.",
    "contentHtml": "<h2>As fases</h2><p>O ciclo dura em média <strong>28 dias</strong>.</p><ul><li>Menstrual</li><li>Folicular</li></ul><p style=\"color:#c2185b\">Período fértil.</p><iframe src=\"https://www.youtube.com/embed/ID\" width=\"560\" height=\"315\"></iframe>",
    "coverImageUrl": null
  }'
```

## Listar e consultar

```bash
curl -H "Authorization: Bearer $USER" http://localhost:8080/api/article
curl -H "Authorization: Bearer $USER" http://localhost:8080/api/article/1
```

A lista vem da mais recente para a mais antiga por `updatedAt`, então o artigo recém-editado aparece no topo.

## Editar

```bash
curl -X PUT http://localhost:8080/api/article/1 \
  -H "Authorization: Bearer $ADMIN" \
  -H 'Content-Type: application/json' \
  -d '{"title":"Ciclo menstrual (revisado)","summary":"Versão editada","contentHtml":"<h2>Conteúdo novo</h2>","coverImageUrl":null}'
```

## Upload de imagem

O editor rico deve chamar esta rota e inserir a URL devolvida no `src` da imagem, em vez de embutir base64 no HTML.

```bash
curl -X POST http://localhost:8080/api/media \
  -H "Authorization: Bearer $ADMIN" \
  -F 'file=@/caminho/para/imagem.png'
```

Resposta:

```json
{"url":"/media/75850497-4697-42ae-97da-ffc8d22cadeb.png"}
```

A leitura não precisa de token:

```bash
curl -i http://localhost:8080/media/75850497-4697-42ae-97da-ffc8d22cadeb.png
```

## Excluir

```bash
curl -i -X DELETE http://localhost:8080/api/article/1 -H "Authorization: Bearer $ADMIN"
```

Responde `204`. Na próxima listagem o artigo já não aparece, e some do app.

## Erros esperados

```bash
# 401 - sem token
curl -i http://localhost:8080/api/article

# 403 - USER tentando escrever
curl -i -X POST http://localhost:8080/api/article \
  -H "Authorization: Bearer $USER" \
  -H 'Content-Type: application/json' \
  -d '{"title":"Não pode","contentHtml":"<p>x</p>"}'

# 404 - artigo inexistente
curl -i -H "Authorization: Bearer $USER" http://localhost:8080/api/article/999

# 400 - campo obrigatório ausente
curl -i -X POST http://localhost:8080/api/article \
  -H "Authorization: Bearer $ADMIN" \
  -H 'Content-Type: application/json' \
  -d '{"summary":"sem titulo nem conteudo"}'
```
