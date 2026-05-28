# Coupon API

API REST para **criação e remoção lógica (soft delete) de cupons**, construída com **Spring Boot 4 + Java 17**, persistência em **H2 em memória** e organização inspirada em **Domain-Driven Design (DDD)**.

A camada de domínio encapsula as regras de negócio (validação, sanitização e ciclo de vida do cupom), mantendo controllers e serviços finos e focados em orquestração.

---

## Sumário

- [Stack](#stack)
- [Arquitetura & DDD](#arquitetura--ddd)
- [Como executar](#como-executar)
  - [Localmente (Maven)](#localmente-maven)
  - [Via Docker](#via-docker)
  - [Via Docker Compose](#via-docker-compose)
- [Endpoints](#endpoints)
- [Regras de negócio](#regras-de-negócio)
- [Tratamento de erros](#tratamento-de-erros)
- [Testes](#testes)
- [Decisões técnicas](#decisões-técnicas)

---

## Stack

| Camada            | Tecnologia                         |
| ----------------- | ---------------------------------- |
| Linguagem         | Java 17                            |
| Framework         | Spring Boot 4.0.6                  |
| Persistência      | Spring Data JPA + H2 (in-memory)   |
| Documentação API  | Springdoc OpenAPI (Swagger UI)     |
| Build             | Maven Wrapper (`mvnw`)             |
| Boilerplate       | Lombok                             |
| Containerização   | Docker + Docker Compose            |
| Testes            | JUnit 5 + Spring Boot Test         |

---

## Arquitetura & DDD

O projeto segue uma separação por **responsabilidade de camada**, isolando a regra de negócio do framework e da infraestrutura:

```
src/main/java/com/cupom/projeto
├── controller/      → Camada de entrada HTTP (REST)
├── dto/             → Contratos de entrada e saída (request/response)
├── services/        → Application Service — orquestra o caso de uso
├── domain/          → Núcleo de domínio (regras e invariantes do Coupon)
├── models/          → Entidade JPA (persistência)
├── repositories/    → Abstração de persistência (Spring Data)
└── exception/       → Exceções de negócio e handler global
```

### Por que esta separação?

- **`domain/CouponDomain`** concentra as **invariantes do agregado Coupon**: validação de código, descrição, valor mínimo, expiração e ciclo de vida (`ACTIVE` → `INACTIVE` via soft delete). É a "fonte da verdade" do negócio e não conhece HTTP nem JPA.
- **`services/CouponService`** atua como **Application Service**: coordena domínio + repositório, traduz DTOs e expõe casos de uso (`createCupom`, `delete`, `findById`).
- **`controller/CouponController`** é apenas adaptador HTTP — não decide nada, só delega.
- **`models/Coupon`** é a representação persistente do agregado, mantida simples (Lombok) para que o `CouponDomain` aplique as regras.
- **`exception/`** centraliza o vocabulário de erro do negócio (`BusinessException`) com um handler global que padroniza a resposta HTTP.

Essa divisão permite que regras de negócio evoluam **sem tocar em controllers ou JPA**, e torna o domínio testável de forma isolada (ver [CouponDomainTest](src/test/java/com/cupom/projeto/CouponDomainTest.java)).

---

## Como executar

### Pré-requisitos

- **Java 17+**
- **Maven** (ou utilize o wrapper `./mvnw`)
- **Docker** (opcional, para container)

### Localmente (Maven)

```bash
./mvnw spring-boot:run
```

A aplicação sobe em `http://localhost:8080`.

> **Windows (PowerShell):** `./mvnw.cmd spring-boot:run`

### Via Docker

Gere o JAR e construa a imagem:

```bash
./mvnw clean package -DskipTests
docker build -t coupon-api .
docker run -p 8080:8080 coupon-api
```

### Via Docker Compose

```bash
./mvnw clean package -DskipTests
docker compose up --build
```

A aplicação é exposta em `http://localhost:8081` (mapeamento `8081:8080` definido em [compose.yaml](compose.yaml)).

---

## Endpoints

Base URL: `http://localhost:8080`

| Método   | Rota             | Descrição                              | Status de sucesso |
| -------- | ---------------- | -------------------------------------- | ----------------- |
| `POST`   | `/coupon`        | Cria um novo cupom                     | `201 Created`     |
| `DELETE` | `/coupon/{id}`   | Remove logicamente um cupom (soft)     | `204 No Content`  |

### Recursos auxiliares

| Recurso        | URL                                         |
| -------------- | ------------------------------------------- |
| Swagger UI     | `http://localhost:8080/swagger-ui.html`     |
| OpenAPI JSON   | `http://localhost:8080/v3/api-docs`         |
| Console H2     | `http://localhost:8080/h2-console`          |

> H2: JDBC URL `jdbc:h2:mem:cupondb`, usuário `sa`, senha em branco.

### Exemplo — `POST /coupon`

**Request**

```json
{
  "code": "ABC123",
  "description": "Cupom de boas-vindas",
  "discountValue": 10.0,
  "expirationDate": "2026-12-31",
  "published": true
}
```

**Response — `201 Created`**

```json
{
  "id": "6f1c5a14-9b3d-4f8a-9e7b-2c1a8e5f0d12",
  "code": "ABC123",
  "description": "Cupom de boas-vindas",
  "discountValue": 10.0,
  "expirationDate": "2026-12-31",
  "status": "ACTIVE",
  "published": true,
  "redeemed": false
}
```

### Exemplo — `DELETE /coupon/{id}`

**Response — `204 No Content`** (cupom é marcado como `deleted = true` e `status = "INACTIVE"`).

---

## Regras de negócio

Aplicadas em [`CouponDomain`](src/main/java/com/cupom/projeto/domain/CouponDomain.java):

- **`code`** — obrigatório; sanitizado removendo caracteres não-alfanuméricos e convertido para maiúsculas; precisa resultar em **exatamente 6 caracteres**.
- **`description`** — obrigatória, não pode ser vazia ou somente espaços.
- **`discountValue`** — obrigatório e **maior ou igual a 0.5**.
- **`expirationDate`** — obrigatória e **não pode ser anterior à data atual**.
- **`published`** — opcional; quando `null`, assume `false`.
- **Soft delete** — `delete()` marca `deleted = true` e `status = "INACTIVE"`. Reexcluir um cupom já deletado lança `BusinessException`. A consulta `findActiveById` ignora cupons deletados.

---

## Tratamento de erros

Implementado em [`GlobalExceptionHandler`](src/main/java/com/cupom/projeto/exception/GlobalExceptionHandler.java). Toda `BusinessException` retorna **HTTP 400** com payload padronizado:

```json
{
  "timestamp": "2026-05-28T14:32:10.123Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Discount value must be at least 0.5"
}
```

---

## Testes

Execute toda a suíte com:

```bash
./mvnw test
```

Cobertura atual:

- [`CouponDomainTest`](src/test/java/com/cupom/projeto/CouponDomainTest.java) — valida invariantes do domínio: criação válida, sanitização de código, valor mínimo, data passada, soft delete e dupla exclusão.
- [`CouponServiceTest`](src/test/java/com/cupom/projeto/CouponServiceTest.java) — valida o Application Service.

---

## Decisões técnicas

| Decisão                                                       | Motivação                                                                                                              |
| ------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------- |
| **Domínio isolado em `CouponDomain` como `@Component`**       | Permite testar regras sem subir o contexto JPA/Web e mantém o serviço de aplicação fino.                              |
| **Soft delete com `deleted` + `status`**                      | Cupons têm valor histórico (auditoria, relatórios); apagar fisicamente perderia rastro. `findActiveById` filtra deletados. |
| **Sanitização do `code`**                                     | Garante consistência (`abc-123` e `ABC123` resultam no mesmo código) antes de validar o tamanho.                       |
| **H2 in-memory**                                              | Foco em demonstrar a regra de negócio sem dependência externa; basta trocar `application.properties` para PostgreSQL/MySQL. |
| **`BusinessException` + `GlobalExceptionHandler`**            | Centraliza a tradução de erros de domínio em respostas HTTP coerentes.                                                |
| **DTOs separados (`CouponCreateRequestDTO` / `CouponResponseDTO`)** | Desacopla o contrato da API da entidade JPA, evitando vazamento de detalhes de persistência.                          |
| **Docker Compose mapeando `8081:8080`**                       | Evita conflito com instância local rodando na 8080 durante desenvolvimento simultâneo.                                 |
| **Lombok**                                                    | Reduz boilerplate em entidades e DTOs sem prejudicar a leitura do domínio.                                            |
