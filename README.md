# 🛵 Delivery API
![CI](https://github.com/CarlosEduardoPCervelli/delivery-api/actions/workflows/ci.yml/badge.svg)

API REST para um sistema de delivery, com cadastro de clientes, restaurantes, produtos e gerenciamento completo do ciclo de vida de um pedido — do recebimento até a entrega.

Projeto desenvolvido para fins de portfólio, aplicando arquitetura em camadas, autenticação stateless com JWT, regras de negócio no back-end e ambiente containerizado com Docker.

## ✨ Funcionalidades

- **Autenticação e autorização** com JWT, suportando três perfis de usuário: `CLIENTE`, `RESTAURANTE` e `ENTREGADOR`
- **Cadastro de restaurantes e produtos**, com controle de disponibilidade e horário de funcionamento
- **Criação de pedidos** com múltiplos itens, onde o valor é sempre recalculado no servidor (o preço nunca é confiado a partir do que o cliente envia)
- **Máquina de estados do pedido**, com transições validadas:

  ```
  RECEBIDO → EM_PREPARO → SAIU_PARA_ENTREGA → ENTREGUE
     ↓            ↓
  CANCELADO   CANCELADO
  ```

- **Fluxo do entregador**: o entregador vê os pedidos em preparo que ainda não têm entregador, aceita um deles (uma entrega por vez) e confirma a entrega no final. O restaurante só consegue despachar o pedido (`SAIU_PARA_ENTREGA`) depois que um entregador o aceitou, e a entrega (`ENTREGUE`) é confirmada pelo entregador responsável
- **Regras de negócio no domínio**: pedido só pode ser criado se o restaurante estiver aberto; produto só pode ser vendido se estiver disponível e pertencer ao restaurante do pedido; transições de status inválidas são bloqueadas; um pedido só pode ser consultado por quem participa dele (cliente, restaurante ou entregador)
- **Paginação e filtros** nas listagens (restaurantes por categoria/nome, pedidos por status)
- **Tratamento global de exceções**, com respostas de erro padronizadas
- **Documentação interativa** via Swagger/OpenAPI

## 🛠️ Stack

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 17 |
| Framework | Spring Boot 3 (Web, Data JPA, Security, Validation) |
| Banco de dados | PostgreSQL |
| Autenticação | JWT (JJWT) + BCrypt |
| Documentação | springdoc-openapi (Swagger UI) |
| Testes | JUnit 5 + Mockito + AssertJ |
| Infraestrutura | Docker + Docker Compose |
| CI | GitHub Actions |

## 🏗️ Arquitetura

O projeto segue arquitetura em camadas:

```
Controller → Service → Repository → Entity
              ↓
             DTO (entrada e saída, nunca expõe a entidade JPA diretamente)
```

- **`model/`** — entidades JPA e enums do domínio
- **`repository/`** — interfaces Spring Data JPA
- **`service/`** — regras de negócio e orquestração de transações
- **`controller/`** — endpoints REST, validação de entrada, autorização por perfil
- **`dto/`** — objetos de transferência de dados, separados por request/response
- **`security/`** — filtro JWT, geração/validação de token, `UserDetailsService`
- **`exception/`** — exceções customizadas e handler global (`@RestControllerAdvice`)

## 🚀 Como rodar

### Com Docker (recomendado)

Pré-requisito: Docker e Docker Compose instalados.

```bash
git clone https://github.com/CarlosEduardoPCervelli/delivery-api.git
cd delivery-api
docker-compose up --build
```

A aplicação sobe em `http://localhost:8080` e o banco PostgreSQL em `localhost:5432`.

### Localmente (sem Docker)

Pré-requisito: Java 17, Maven e um PostgreSQL local rodando (ajuste `application.yml` se necessário).

```bash
mvn clean install
mvn spring-boot:run
```

### Rodando os testes

```bash
mvn test
```

## 📖 Documentação da API

Com a aplicação rodando, acesse o Swagger UI:

```
http://localhost:8080/docs
```

## 🔑 Fluxo básico de uso

1. **Cadastrar um restaurante**: `POST /auth/register` com `perfil: RESTAURANTE`
2. **Cadastrar um cliente**: `POST /auth/register` com `perfil: CLIENTE`
3. **Login**: `POST /auth/login` → retorna o token JWT
4. **Restaurante cadastra produtos**: `POST /restaurantes/me/produtos` (com o token do restaurante)
5. **Cliente cria um pedido**: `POST /pedidos` (com o token do cliente)
6. **Restaurante avança o status do pedido** (`RECEBIDO` → `EM_PREPARO`): `PATCH /pedidos/{id}/status`
7. **Cadastrar um entregador**: `POST /auth/register` com `perfil: ENTREGADOR` e `veiculo`
8. **Entregador lista e aceita um pedido em preparo**: `GET /pedidos/entregas/disponiveis` e `PATCH /pedidos/{id}/aceitar-entrega`
9. **Restaurante despacha o pedido** (`SAIU_PARA_ENTREGA`): `PATCH /pedidos/{id}/status`
10. **Entregador confirma a entrega**: `PATCH /pedidos/{id}/confirmar-entrega`

Todas as rotas protegidas esperam o header:
```
Authorization: Bearer <token>
```

## 📌 Principais endpoints

| Método | Rota | Descrição | Acesso |
|---|---|---|---|
| POST | `/auth/register` | Cadastra usuário | Público |
| POST | `/auth/login` | Autentica e retorna token | Público |
| GET | `/restaurantes` | Lista restaurantes (filtros: `categoria`, `nome`) | Público |
| GET | `/restaurantes/{id}/produtos` | Lista produtos de um restaurante | Público |
| PUT | `/restaurantes/me` | Atualiza perfil do restaurante logado | RESTAURANTE |
| POST | `/restaurantes/me/produtos` | Cadastra produto | RESTAURANTE |
| PUT | `/produtos/{id}` | Atualiza produto | RESTAURANTE (dono) |
| DELETE | `/produtos/{id}` | Remove produto | RESTAURANTE (dono) |
| POST | `/pedidos` | Cria um pedido | CLIENTE |
| GET | `/pedidos/me` | Lista pedidos do cliente logado | CLIENTE |
| GET | `/pedidos/restaurante/me` | Lista pedidos recebidos (filtro: `status`) | RESTAURANTE |
| GET | `/pedidos/{id}` | Detalhes de um pedido | CLIENTE, RESTAURANTE ou ENTREGADOR do pedido |
| PATCH | `/pedidos/{id}/status` | Atualiza status do pedido (a confirmação de `ENTREGUE` é do entregador) | RESTAURANTE |
| GET | `/pedidos/entregas/disponiveis` | Lista pedidos em preparo sem entregador | ENTREGADOR |
| GET | `/pedidos/entregas/me` | Lista as entregas do entregador logado | ENTREGADOR |
| PATCH | `/pedidos/{id}/aceitar-entrega` | Aceita um pedido em preparo | ENTREGADOR |
| PATCH | `/pedidos/{id}/confirmar-entrega` | Confirma a entrega do pedido | ENTREGADOR (responsável) |

## 🗺️ Possíveis evoluções

- Localização e acompanhamento em tempo real do entregador
- Avaliações de restaurante e produto
- Notificações em tempo real (WebSocket) sobre mudança de status
- Testes de integração com Testcontainers

## 📄 Licença

Projeto livre para fins de estudo e portfólio.
