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
| `UserController` | Rotas de autenticação e o CRUD de usuários em `/api/user`. Escrita e leitura restritas a `ADMIN`, exceto `login` e `register` |

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
| `UserService` | Operações de usuário: registrar, criar pela gestão, editar, excluir logicamente, listar, listar administradores e buscar. Faz o encode da senha e resolve o papel, criando-o na primeira vez que é usado. `@Transactional(readOnly = true)` na classe, sobrescrito nos métodos de escrita |

## `service.authorization`

Integração entre o domínio e o Spring Security.

| Classe | Responsabilidade |
|---|---|
| `Authorization` | Implementa `UserDetailsService`. Carrega o usuário pelo e-mail para o `DaoAuthenticationProvider`, lançando `UsernameNotFoundException` quando não encontra |

## `service.article`

| Classe | Responsabilidade |
|---|---|
| `ArticleService` | CRUD de artigos, com exclusão lógica. Lista da mais recente para a mais antiga por `updatedAt`, para o app refletir as edições no topo. Lança `NotFoundException` quando o id não existe |

## `service.media`

| Classe | Responsabilidade |
|---|---|
| `MediaService` | Grava o arquivo em disco com nome `UUID`, preservando a extensão, e devolve a URL de leitura |

## `repository.user`

Acesso a dados. Interfaces `JpaRepository`, com implementação gerada pelo Spring Data.

| Classe | Responsabilidade |
|---|---|
| `UserRepository` | `JpaRepository<UserModel, Long>`. Consultas derivadas que filtram `deletedAt is null`, mais `existsByEmail` (que considera os excluídos, mantendo o e-mail reservado) e `existsByEmailAndIdNot` (usada na edição) |
| `RoleRepository` | `JpaRepository<RoleModel, Long>`. Consulta derivada `findByName` |

## `repository.article`

| Classe | Responsabilidade |
|---|---|
| `ArticleRepository` | `JpaRepository<ArticleModel, Long>`. Consultas derivadas que filtram `deletedAt is null`, ordenando por `updatedAt` decrescente |

## `model.user`

Entidades JPA e tipos do domínio.

| Classe | Responsabilidade |
|---|---|
| `UserModel` | Entidade da tabela `TB_USER`. Implementa `UserDetails`, convertendo o papel em authorities do Spring Security. `ADMIN` acumula `ROLE_ADMIN` e `ROLE_USER`. `deletedAt` marca a exclusão lógica |
| `RoleModel` | Entidade da tabela `TB_ROLE`. Isola o papel de acesso, referenciado por `TB_USER.role_id` |

## `model.article`

| Classe | Responsabilidade |
|---|---|
| `ArticleModel` | Entidade da tabela `TB_ARTICLE`. O `contentHtml` é `TEXT` e guarda o HTML do editor rico. `createdAt` e `updatedAt` são preenchidos pelo Hibernate. `deletedAt` marca a exclusão lógica |

## `dto.user`

Contratos de entrada e saída da API, como records. Isola a entidade do corpo das requisições e respostas.

| Classe | Responsabilidade |
|---|---|
| `UserDto` | Entrada de criação. No `POST /api/user` (gestão) o papel enviado é gravado; no `POST /api/user/register` (público) ele é ignorado e vale sempre `USER` |
| `UserUpdateDto` | Entrada da edição: nome, e-mail e papel. A senha não é alterada por aqui |
| `LoginDto` | Entrada do login |
| `LoginResponseDto` | Saída do login: o usuário autenticado (`UserResponseDto`) e o token. O usuário vai junto para o cliente não precisar de uma segunda chamada só para saber quem entrou |
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
| `POST` | `/api/user` | `ADMIN` |
| `GET` | `/api/user` | `ADMIN` |
| `GET` | `/api/user/admins` | `ADMIN` |
| `GET` | `/api/user/{id}` | `ADMIN` |
| `PUT` | `/api/user/{id}` | `ADMIN` |
| `DELETE` | `/api/user/{id}` | `ADMIN` |
| `GET` | `/api/article` | autenticado |
| `GET` | `/api/article/{id}` | autenticado |
| `POST` | `/api/article` | `ADMIN` |
| `PUT` | `/api/article/{id}` | `ADMIN` |
| `DELETE` | `/api/article/{id}` | `ADMIN` |
| `POST` | `/api/media` | `ADMIN` |
| `GET` | `/media/{arquivo}` | público |

`GET /media/**` é público porque a tag `<img>` não envia o header `Authorization`. Exigir token nessa rota faria toda imagem quebrar na web e no app. A proteção é o nome ser um `UUID` aleatório, e só `ADMIN` conseguir criar arquivos.

Há duas rotas de criação de usuário porque o papel exige permissões diferentes:

- `POST /api/user/register` é **pública** e grava sempre `USER`. É o cadastro do aplicativo. O campo `userRole` enviado aqui é ignorado.
- `POST /api/user` exige **`ADMIN`** e grava o papel enviado no corpo. É o cadastro da gestão web, o único caminho para criar outro administrador.

## Exclusão lógica

`DELETE` de usuário e de artigo **não apaga o registro**: preenche a coluna `deleted_at` com o horário da remoção. Todas as consultas de leitura filtram `deleted_at is null`, então o excluído desaparece das listagens e responde `404` quando buscado pelo id.

No caso do usuário, a exclusão também corta o acesso na hora:

- ele não consegue mais fazer login;
- o token que ele já tinha em mãos passa a responder `401`, porque o filtro de segurança ignora usuários excluídos;
- o e-mail dele continua reservado e não pode ser reaproveitado num novo cadastro.

---

# Como rodar

```bash
docker compose up -d
./mvnw spring-boot:run
```

A aplicação sobe em `http://localhost:8080` e o Hibernate cria o schema (`spring.jpa.hibernate.ddl-auto=update`).

## Carga inicial

O arquivo `src/main/resources/data.sql` roda a cada inicialização, depois do Hibernate criar as tabelas, e insere os papéis e dois usuários. É idempotente: só grava o que ainda não existe, então reiniciar não duplica nada.

| E-mail | Senha | Papel |
|---|---|---|
| `admin@saudefeminina.com` | `admin123` | `ADMIN` |
| `maria@saudefeminina.com` | `maria123` | `USER` |

O administrador precisa vir da carga porque `POST /api/user`, o único caminho para criar um `ADMIN`, exige um administrador já autenticado.

Para acessar o app do emulador Android, use `http://10.0.2.2:8080` no lugar de `localhost` — no emulador, `localhost` é o próprio dispositivo.

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

Resposta:

```json
{
  "user": {
    "id": 1,
    "name": "Leandro Zanella",
    "email": "admin@saudefeminina.com",
    "role": "ADMIN",
    "createdAt": "2026-08-26T20:31:04.221"
  },
  "token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9..."
}
```

## Usuários (todas exigem `ADMIN`)

```bash
# lista os ativos
curl -H "Authorization: Bearer $ADMIN" http://localhost:8080/api/user

# lista só os administradores
curl -H "Authorization: Bearer $ADMIN" http://localhost:8080/api/user/admins

# busca por id
curl -H "Authorization: Bearer $ADMIN" http://localhost:8080/api/user/1
```

Criar um administrador — só a gestão web consegue, porque a rota exige `ADMIN`:

```bash
curl -X POST http://localhost:8080/api/user \
  -H "Authorization: Bearer $ADMIN" \
  -H 'Content-Type: application/json' \
  -d '{"name":"Nova Admin","email":"nova@saudefeminina.com","password":"senha123","userRole":"ADMIN"}'
```

Editar. O corpo não leva senha; trocar apenas o nome, mantendo o mesmo e-mail, funciona normalmente:

```bash
curl -X PUT http://localhost:8080/api/user/2 \
  -H "Authorization: Bearer $ADMIN" \
  -H 'Content-Type: application/json' \
  -d '{"name":"Maria Souza Silva","email":"maria@saudefeminina.com","userRole":"USER"}'
```

Excluir. Responde `204`, e o registro continua no banco com `deleted_at` preenchido:

```bash
curl -i -X DELETE http://localhost:8080/api/user/2 -H "Authorization: Bearer $ADMIN"

# some da listagem
curl -H "Authorization: Bearer $ADMIN" http://localhost:8080/api/user

# e não consegue mais entrar
curl -i -X POST http://localhost:8080/api/user/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"maria@saudefeminina.com","password":"maria123"}'
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

Responde `204`. É exclusão lógica: o artigo continua na tabela com `deleted_at` preenchido, mas some da listagem e do app, e passa a responder `404` quando buscado pelo id.

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

# 403 - USER tentando acessar a gestão de usuários
curl -i -H "Authorization: Bearer $USER" http://localhost:8080/api/user/admins

# 409 - e-mail de outro usuário na edição
curl -i -X PUT http://localhost:8080/api/user/2 \
  -H "Authorization: Bearer $ADMIN" \
  -H 'Content-Type: application/json' \
  -d '{"name":"Maria","email":"admin@saudefeminina.com","userRole":"USER"}'

# 400 - campo obrigatório ausente
curl -i -X POST http://localhost:8080/api/article \
  -H "Authorization: Bearer $ADMIN" \
  -H 'Content-Type: application/json' \
  -d '{"summary":"sem titulo nem conteudo"}'
```
