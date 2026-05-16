# Ironvault Payments Monorepo

Monorepo inicial com dois projetos:

- `backend/`: API em Spring Boot com base para arquitetura hexagonal.
- `frontend/`: aplicação React para interface do sistema de pagamentos.

## Objetivo desta fase

Este setup é **intencionalmente inicial** para suportar implementação faseada:

- estrutura de pastas e responsabilidades principais;
- configuração mínima para execução local;
- pontos de extensão para evolução por etapas.

## Estrutura

```text
.
├── backend
│   ├── pom.xml
│   └── src/main/java/com/ironvault/payments
│       ├── adapter
│       ├── application
│       └── domain
└── frontend
    ├── package.json
    ├── vite.config.ts
    └── src
```

## Como executar

### Backend

```bash
cd backend
mvn spring-boot:run
```

### Frontend

```bash
cd frontend
npm install
npm run dev
```

## Próximos passos sugeridos

1. Definir casos de uso prioritários no `domain/port/in`.
2. Implementar adaptadores de persistência (`adapter/out`) com banco escolhido.
3. Definir cliente HTTP no frontend para comunicação com backend.
4. Incluir autenticação/autorização e observabilidade.
