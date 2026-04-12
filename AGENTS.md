# FreightFlow API — AGENTS.md

## Objetivo
Este projeto é o backend do FreightFlow, construído com Java 21 + Spring Boot 3.3.
A prioridade é evolução incremental, segurança multi-tenant, consistência de domínio e estabilidade operacional.

## Regras de trabalho
- Não quebrar comportamento existente sem necessidade clara.
- Preferir mudanças pequenas, sequenciais e verificáveis.
- Não reescrever grandes blocos se um ajuste localizado resolver.
- Não espalhar regra de negócio entre controller, service e repository.
- Controllers devem permanecer finos.
- Services concentram casos de uso e regras de domínio.
- Repositories não devem conter regra de apresentação.
- DTOs devem refletir contratos claros e estáveis.

## Multi-tenant e segurança
- Toda operação autenticada deve respeitar tenant isolation.
- Quando aplicável, role CLIENT deve ser filtrada também por customerId do caller.
- Nunca confiar em filtros vindos apenas do frontend.
- O backend é a fonte de verdade para autorização, escopo e visibilidade de dados.

## Fleet Map / Tracking
- O backend é a fonte de verdade para:
    - riskLevel
    - shipmentCount
    - carrier, quando possível
    - origem da posição do navio
- Não deixar o frontend inferir regra de negócio sensível.
- Sempre tratar falha de provider externo sem quebrar o endpoint principal.
- Quando AIS falhar, usar política explícita e consistente:
    - LIVE
    - CACHED
    - ESTIMATED
    - UNAVAILABLE

## Alertas e analytics
- Evitar duplicação de lógica entre dashboard, alertas e listagens.
- Antes de criar scheduler ou lógica periódica, revisar idempotência e risco de duplicidade.
- Analytics operacionais devem priorizar coerência com o domínio e queries sustentáveis.

## Performance e resiliência
- Evitar N+1.
- Revisar queries agregadas antes de iterar em loops com acesso ao banco.
- Proteger integrações externas com timeout, fallback e tratamento de erro.
- Não deixar falha de serviço externo derrubar a API quando houver alternativa segura.

## Testes e validação
Antes de concluir qualquer etapa relevante:
- identificar o comando de build/compile existente no projeto
- identificar o comando de testes existente no projeto
- executar ao menos compile/build da parte alterada
- quando possível, adicionar testes mínimos cobrindo comportamento crítico

## Estilo de execução esperado
1. Ler os arquivos relevantes antes de editar.
2. Resumir diagnóstico curto.
3. Informar quais arquivos serão alterados.
4. Implementar em sequência.
5. Validar build/compile.
6. Resumir o que foi feito, riscos e próximos passos.

## Uso de subagentes
- Subagentes somente para leitura, exploração ou checklist.
- Não usar subagentes para escrever backend em paralelo.
- O agente principal consolida a análise e implementa.