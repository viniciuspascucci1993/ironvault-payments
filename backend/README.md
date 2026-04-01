---

## Roadmap de evolução do fluxo de pagamentos (idempotência + gateway)

Este roadmap detalha a evolução do fluxo atual para um modelo pronto para integração real com provedores como Stripe/Adyen, mantendo entregas pequenas e seguras.

### Estado atual (referência)

- Criação de pagamento inicia com status `CREATED`.
- Já existem status no domínio para ciclo completo (`PROCESSING`, `APPROVED`, `REJECTED`, `FAILED`).
- Fluxo de idempotência já reserva chave e persiste vínculo com `payment_id`.

### PR 1 — Orquestração de status (base de domínio)

**Objetivo:** formalizar transições válidas de status sem dependência de gateway.

**Escopo:**
- Criar serviço/use case de atualização de status com regras de transição.
- Validar transições permitidas:
- `CREATED -> PROCESSING`
- `PROCESSING -> APPROVED | REJECTED | FAILED`
- Bloquear transições inválidas (ex.: `APPROVED -> PROCESSING`).
- Persistir `updatedAt` e `failureReason` quando aplicável.
- Cobrir com testes unitários e de integração.

**Critério de aceite:**
- Não é possível forçar transições inválidas via camada de aplicação.
- Logs e payloads retornam status coerente.

### PR 2 — Processamento assíncrono (mock provider)

**Objetivo:** separar criação do pagamento da confirmação final.

**Escopo:**
- No create:
- mantém criação com `CREATED`,
- avança para `PROCESSING`,
- dispara processamento assíncrono (worker/job).
- Introduzir `PaymentGatewayPort` no domínio.
- Implementar provider mock (simulando aprovação/rejeição/falha).
- Persistir `externalId` retornado pelo provider.
- Cobrir cenário happy path e erro técnico.

**Critério de aceite:**
- API responde rápido no create.
- Atualização de status acontece de forma assíncrona e rastreável.

### PR 3 — Webhooks + idempotência de eventos (produção)

**Objetivo:** preparar integração real com gateway.

**Escopo:**
- Expor endpoint de webhook para eventos de pagamento.
- Validar assinatura do provedor (segurança).
- Garantir idempotência de webhook (`event_id` único/processado).
- Reconciliar status por evento recebido.
- Adicionar logs estruturados por `paymentId`, `externalId` e `eventId`.
- Cobrir testes de assinatura inválida, duplicidade e eventos fora de ordem.

**Critério de aceite:**
- Mesmo webhook duplicado não gera efeitos colaterais.
- Status final do pagamento converge com eventos do gateway.

### Pós-roadmap (integração real Stripe/Adyen)

Após os 3 PRs acima:
- substituir provider mock por implementação real (`StripeGatewayAdapter` ou `AdyenGatewayAdapter`);
- configurar segredos por ambiente;
- ativar observabilidade (métricas, tracing, alertas);
- validar estratégia de retry, timeout e circuit breaker.

### Ordem recomendada de execução

1. PR 1 (regras de estado).
2. PR 2 (assíncrono com mock).
3. PR 3 (webhook + idempotência de eventos).
4. Integração real com provedor.