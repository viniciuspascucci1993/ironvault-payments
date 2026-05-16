# Estrutura Refinada — Backend Hexagonal (Spring Boot)

## Visão Geral (Maven Multi-module)

```text
backend/                            # parent project (packaging: pom)
├─ pom.xml
├─ modules/
│  ├─ domain/
│  │  ├─ pom.xml
│  │  └─ src/main/java/com/ironvault/payments/domain/
│  │     ├─ model/
│  │     ├─ valueobject/
│  │     ├─ service/
│  │     ├─ event/
│  │     └─ exception/
│  │
│  ├─ application/
│  │  ├─ pom.xml
│  │  └─ src/main/java/com/ironvault/payments/application/
│  │     ├─ usecase/
│  │     ├─ port/
│  │     │  ├─ in/
│  │     │  └─ out/
│  │     ├─ dto/
│  │     └─ mapper/
│  │
│  ├─ infrastructure/
│  │  ├─ pom.xml
│  │  └─ src/main/java/com/ironvault/payments/infrastructure/
│  │     ├─ adapter/
│  │     │  ├─ in/
│  │     │  │  ├─ rest/
│  │     │  │  └─ webhook/
│  │     │  └─ out/
│  │     │     ├─ persistence/
│  │     │     │  └─ postgres/
│  │     │     ├─ messaging/
│  │     │     │  ├─ kafka/
│  │     │     │  └─ rabbitmq/
│  │     │     ├─ pix/
│  │     │     └─ security/
│  │     └─ config/
│  │
│  └─ boot/
│     ├─ pom.xml
│     └─ src/main/java/com/ironvault/payments/boot/
│        └─ PaymentsApplication.java
└─ README.md
```

## Renomeações aplicadas

- `iv-domain` → `domain`
- `iv-application` → `application`
- `iv-infrastructure` → `infrastructure`
- `iv-boot` → `boot`
- `pixprovider` → `pix`
- `messaging/kafka` (ou `messaging/rabbitmq`, conforme stack escolhida)
- `persistence/postgres`

## Dependências entre módulos (regra de ouro)

- `domain`: **não depende** de frameworks (Spring, JPA, Kafka etc.).
- `application`: depende **apenas** de `domain`.
- `infrastructure`: depende de `application` e `domain`.
- `boot`: módulo de inicialização/composição; depende de `infrastructure` (e indiretamente dos demais).

## Responsabilidades por módulo

### 1) `domain`

Camada de regras de negócio puras e linguagem ubíqua.

- **`model/`**: entidades e agregados (ex.: `Payment`, `Transfer`, `Refund`).
- **`valueobject/`**: objetos-valor imutáveis (ex.: `Money`, `PixKey`, `DocumentNumber`).
- **`service/`**: serviços de domínio para regras que não pertencem naturalmente a uma única entidade.
- **`event/`**: eventos de domínio (ex.: `PaymentRequested`, `PaymentSettled`).
- **`exception/`**: exceções de negócio e violações de invariantes.

> Não contém anotações/frameworks de infraestrutura.

### 2) `application`

Orquestra casos de uso e define contratos (ports).

- **`usecase/`**: implementação dos fluxos de aplicação (ex.: `CreatePaymentUseCase`).
- **`port/in/`**: portas de entrada (interfaces chamadas pelos adapters de entrada).
- **`port/out/`**: portas de saída (interfaces para persistência, mensageria, serviços externos etc.).
- **`dto/`**: contratos de entrada/saída do caso de uso (request/response internos da aplicação).
- **`mapper/`**: conversões entre DTOs e modelos de domínio.

> Não conhece detalhes de REST, banco específico, broker ou provider externo.

### 3) `infrastructure`

Implementa adapters e integrações técnicas.

- **`adapter/in/rest/`**: controllers REST, validação HTTP, serialização.
- **`adapter/in/webhook/`**: endpoints de webhook e normalização de payload externo.
- **`adapter/out/persistence/postgres/`**: repositórios JPA/JDBC, entidades de persistência, mapeamentos SQL.
- **`adapter/out/messaging/kafka/`** ou **`rabbitmq/`**: publishers/consumers.
- **`adapter/out/pix/`**: cliente para integração com provedor Pix.
- **`adapter/out/security/`**: provedores de autenticação/autorização, hash, tokens etc.
- **`config/`**: configuração Spring (beans, clients, properties binding, wiring técnico).

### 4) `boot`

Camada de inicialização da aplicação.

- Classe principal Spring Boot (`PaymentsApplication`).
- Importa configuração e sobe contexto.
- Pode concentrar configurações de runtime/perfil (dev/hml/prod) sem poluir as outras camadas.

## Exemplo de organização de pacotes Java

Base package sugerido:

- `groupId`: `com.ironvault`
- package base: `com.ironvault.payments`

Exemplos:

- `com.ironvault.payments.domain.model.Payment`
- `com.ironvault.payments.domain.valueobject.Money`
- `com.ironvault.payments.application.port.in.CreatePaymentInputPort`
- `com.ironvault.payments.application.usecase.CreatePaymentUseCase`
- `com.ironvault.payments.infrastructure.adapter.in.rest.PaymentController`
- `com.ironvault.payments.infrastructure.adapter.out.persistence.postgres.PaymentJpaRepository`
- `com.ironvault.payments.boot.PaymentsApplication`

## Observações de implementação (para próxima etapa)

- Manter inversão de dependência via interfaces em `application.port.out`.
- Garantir testes por camada:
  - `domain`: testes unitários puros.
  - `application`: testes de caso de uso com portas mockadas.
  - `infrastructure`: testes de integração (banco, broker, web).
- Centralizar anti-corruption/adaptação a provedores externos em `infrastructure.adapter.out`.
- Evitar “anêmico” no domínio: invariantes devem morar no `domain`.

