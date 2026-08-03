# Plano de execução — Comunidade DevNest

> Plano técnico para avaliação. Este arquivo organiza a implementação; não indica que as funcionalidades já existem.

Documento de produto relacionado: [COMMUNITY.md](COMMUNITY.md).

Modelagem do primeiro incremento: [COMMUNITY_MODEL.md](COMMUNITY_MODEL.md).

Este plano também está referenciado no [README principal](../README.md), que funciona como índice da documentação do backend.

## Checklist de acompanhamento

Atualizado em 24/07/2026, após a conclusão das proteções anti-spam e dos testes concorrentes.

Legenda:

- `[x]` implementado e coberto por testes;
- `[ ]` pendente;
- itens parcialmente entregues permanecem desmarcados e detalham o que falta.

### Fase 0 — contratos e fundação

- [x] Definir status e tipos usados por fóruns, posts, conteúdo e reações.
- [x] Disponibilizar `Clock` injetável para regras temporais.
- [x] Definir contratos e configuração do filtro de conteúdo.
- [x] Padronizar exceptions da comunidade e respostas HTTP.
- [x] Consolidar os limites atuais da comunidade em propriedades validadas.
- [ ] Criar fixtures/builders compartilhados para reduzir duplicação nos testes.

### Fase 1 — fóruns, posts e feed

- [x] Criar migration de fóruns, tags e posts (`V7`).
- [x] Implementar entidades, repositories, DTOs e mappers.
- [x] Implementar feed geral e feed por fórum com paginação.
- [x] Implementar criação, consulta, edição e exclusão lógica de posts.
- [x] Aplicar ownership e limite de cinco posts em 24 horas.
- [x] Expor endpoints administrativos de criação, edição e arquivamento de fóruns.
- [x] Garantir o limite de posts sob requisições concorrentes.
- [ ] Validar ausência de N+1 nas queries principais.

### Fase 2 — comentários e conteúdo seguro

- [x] Criar migration de comentários (`V8`).
- [x] Implementar criação, paginação, edição e exclusão lógica.
- [x] Aplicar ownership e bloqueio de interação por estado do post.
- [x] Aplicar o filtro de linguagem a posts e comentários.
- [x] Reter conteúdo sinalizado fora das listagens públicas.
- [ ] Implementar sanitização HTML de segurança.
- [ ] Definir e aplicar a política de URLs, encurtadores e domínios.
- [ ] Adicionar testes HTTP específicos dos endpoints de comentários.

### Fase 3 — reações, bloqueios e anti-spam

- [x] Confirmar os tipos `LIKE`, `HELPFUL`, `CELEBRATE` e `INSIGHTFUL`.
- [x] Criar migration e constraints de reações (`V9`).
- [x] Implementar reações em posts e comentários.
- [x] Implementar troca sem duplicação e remoção idempotente.
- [x] Implementar contadores agregados e reação atual do usuário.
- [x] Bloquear reações em conteúdo indisponível.
- [x] Implementar bloqueio e desbloqueio de usuários.
- [x] Implementar silenciamento e dessilenciamento de usuários.
- [x] Filtrar usuários silenciados no feed pessoal.
- [x] Impedir interações entre usuários bloqueados.
- [x] Implementar rate limits e detecção de conteúdo duplicado.
- [x] Adicionar testes concorrentes de reações contra PostgreSQL.

### Fase 4 — denúncias, trust score e moderação

- [ ] Fechar as decisões bloqueantes da fase.
- [x] Implementar denúncias e fila administrativa.
- [ ] Implementar casos e ações de moderação auditáveis.
- [ ] Implementar trust score, sanções, recuperação e recursos.
- [ ] Implementar lista interna de bloqueio.

### Fase 5 — chat privado

- [ ] Fechar as decisões bloqueantes da fase.
- [ ] Implementar persistência de conversas e mensagens.
- [ ] Implementar endpoints REST de conversa e histórico.
- [ ] Implementar autenticação e autorização WebSocket.
- [ ] Implementar entrega online, histórico offline e leitura.

### Fase 6 — integração e entrega

- [ ] Revisar regras cruzadas de bloqueio, suspensão e banimento.
- [ ] Validar migrations e constraints em PostgreSQL real.
- [ ] Analisar queries, índices, paginação e N+1.
- [ ] Executar testes de carga básicos.
- [ ] Finalizar documentação REST, WebSocket e configuração.
- [ ] Corrigir a falha legada de exclusão administrativa de curso na suíte completa.

## 1. Objetivo

Implementar o backend da comunidade do DevNest seguindo os padrões atuais do projeto:

- organização por domínio e camada;
- DTOs de request e response;
- controllers REST e WebSocket;
- services com regras de negócio;
- access services para ownership e autorização por recurso;
- repositories Spring Data JPA;
- entities JPA e enums;
- migrations Flyway;
- exceptions tratadas pelo `GlobalExceptionHandler`;
- testes de regras, autorização, persistência e fluxos completos.

## 2. Escopo confirmado

- fóruns temáticos reutilizando o domínio de posts;
- feed geral e feed por fórum;
- posts e comentários;
- curtidas e reações;
- bloqueio e silenciamento de usuários;
- denúncias de posts e comentários;
- moderação humana, sem IA;
- exclusão lógica;
- sanções progressivas e recursos;
- trust score percentual;
- suspensão automática em `50%` ou menos;
- banimento permanente após revisão quando confirmado em `35%` ou menos;
- recuperação de `10` pontos percentuais a cada sete dias sem infração confirmada;
- filtro de linguagem com normalização e mapa de substituições;
- sanitização de entrada;
- limite de cinco posts por usuário em janela móvel de 24 horas;
- chat textual privado em tempo real com WebSocket;
- histórico e mensagens offline.

## 3. Fora do escopo da primeira execução

- chamadas de áudio ou vídeo;
- chat público ou em grupo;
- feed personalizado por algoritmo avançado;
- medalhas;
- trends;
- desafios;
- anexos e upload de imagens;
- broker externo para múltiplas instâncias;
- notificações fora do chat.

Medalhas, trends e desafios permanecem no backlog do produto.

## 4. Decisões bloqueantes

Estas decisões devem ser fechadas antes da fase indicada.

| Decisão | Necessária antes de |
|---|---|
| Fórmula e pesos de redução do trust score | Fase 4 |
| Penalidade causada por bad words confirmadas | Fase 4 |
| Categorias e dicionário inicial de termos | Fase 4 |
| Quem visualiza o percentual do trust score | Fase 4 |
| Limite de denúncias | Fase 4 |
| Se denúncias distintas ocultam conteúdo preventivamente | Fase 4 |
| Regras de URLs, encurtadores e domínios | Fase 2 |
| Prazo e processo de recurso | Fase 4 |
| Retenção de conteúdo excluído e lista de bloqueio | Fase 4 |
| Edição e exclusão de mensagens privadas | Fase 5 |
| Confirmação de entrega, leitura e indicador de digitação | Fase 5 |
| Limite de mensagens por minuto | Fase 5 |
| Retenção do histórico do chat | Fase 5 |

Decisões não fechadas não devem ser resolvidas com valores definitivos hardcoded. Usar propriedades configuráveis quando houver um padrão seguro provisório.

## 5. Estratégia de entrega

```text
Fase 0 — contratos e fundação
    ↓
Fase 1 — fóruns, posts e feed
    ↓
Fase 2 — comentários e conteúdo seguro
    ↓
Fase 3 — reações, bloqueios e anti-spam
    ↓
Fase 4 — denúncias, trust score e moderação
    ↓
Fase 5 — chat privado em tempo real
    ↓
Fase 6 — integração, performance e documentação
```

Cada fase deve terminar com testes passando e migration compatível com as anteriores. Não acumular todas as migrations e testes para o final.

## 5.1. Regras de negócio por nível

O nível representa complexidade de implementação, risco e esforço de teste — não a importância da regra.

### Nível 1 — Básicas

Regras locais, normalmente verificadas com dados do request e uma consulta simples.

#### Fóruns e posts

- somente usuário autenticado cria posts;
- título e conteúdo são obrigatórios;
- título e conteúdo respeitam limites configurados;
- o fórum precisa existir e estar ativo;
- cada post pertence a exatamente um fórum;
- o autor pode editar o próprio post;
- o autor pode solicitar a exclusão do próprio post;
- conteúdo removido não aceita novas interações;
- fórum arquivado não aceita novos posts;
- slugs de fóruns são únicos e normalizados;
- entidades JPA nunca são retornadas diretamente.

#### Comentários

- comentário pertence a um único post;
- conteúdo do comentário é obrigatório;
- apenas o autor edita ou exclui o próprio comentário;
- não comentar em post inexistente, removido ou com comentários bloqueados;
- comentário excluído logicamente não aceita reação;
- conteúdo excluído permanece disponível apenas à moderação autorizada.

#### Reações

- tipo da reação precisa ser válido;
- alvo precisa existir e estar ativo;
- reação aponta para um post ou comentário, nunca ambos;
- remover reação inexistente é operação idempotente;
- reagir ao próprio conteúdo não gera benefício de trust score.

#### Denúncias

- denúncia possui motivo válido;
- descrição opcional respeita limite de tamanho;
- denúncia aponta para exatamente um conteúdo;
- o denunciante não visualiza dados internos da revisão;
- denúncia não altera conteúdo ou score diretamente sem regra definida.

#### Chat

- mensagem possui conteúdo não vazio;
- remetente é obtido da autenticação, nunca do payload;
- destinatário precisa existir e estar ativo;
- usuário não inicia conversa consigo mesmo;
- conversa privada possui exatamente dois participantes;
- mensagem pertence a uma conversa existente;
- somente participantes consultam o histórico.

#### Testes esperados

- unitários de validação e status;
- services com casos felizes e recurso inexistente;
- autorização simples de autor e participante;
- validação dos DTOs e formato de erro.

### Nível 2 — Médias

Regras que cruzam entidades, dependem de estado anterior, tempo, paginação ou múltiplas consultas.

#### Feed e fóruns

- feed exibe somente conteúdo ativo e acessível;
- feed por fórum não mistura posts de outros fóruns;
- usuário silenciado não aparece no feed pessoal;
- ordenação é estável por data e ID;
- paginação não repete nem perde registros em condições normais;
- fórum arquivado preserva histórico e leitura;
- administradores gerenciam fóruns, usuários comuns não.

#### Posts e comentários

- usuário cria no máximo cinco posts em janela móvel de 24 horas;
- edição não altera autor, fórum ou data de criação indevidamente;
- exclusão lógica preserva contexto e evidência;
- sanitização ocorre antes da persistência;
- filtro de linguagem analisa cópia normalizada sem alterar o original aprovado;
- conteúdo pode ser aprovado, rejeitado ou retido conforme a regra vigente;
- URLs obedecem protocolos e política configurados.

#### Reações e contadores

- uma conta mantém no máximo uma reação ativa por alvo;
- nova reação substitui a anterior;
- remoção desfaz seu efeito agregado;
- contadores refletem apenas interações ativas;
- conteúdo oculto ou removido não recebe novas reações;
- atividade própria não aumenta reputação;
- contadores são obtidos sem carregar coleções completas.

#### Bloqueio e silenciamento

- bloqueio impede novas interações diretas e mensagens;
- silenciamento afeta somente a visão de quem silenciou;
- bloquear e silenciar não reduzem trust score automaticamente;
- remover bloqueio não restaura mensagens ou interações apagadas;
- moderação autorizada continua vendo evidências necessárias;
- presença e leitura não expõem informações a usuário bloqueado.

#### Denúncias e moderação

- um usuário não denuncia o mesmo alvo mais de uma vez;
- somente administradores consultam a fila completa;
- conteúdo pode transitar entre ativo, oculto e removido conforme ações válidas;
- descarte de denúncia registra motivo e revisor;
- recurso referencia uma sanção existente;
- quando possível, outro moderador analisa o recurso;
- abuso confirmado do sistema de denúncias gera sanção progressiva.

#### Chat

- iniciar conversa com o mesmo par retorna a conversa existente;
- mensagens são persistidas antes da confirmação ao remetente;
- usuário offline recebe o histórico ao reconectar;
- histórico é paginado e ordenado de forma estável;
- mensagens passam por sanitização e filtro de linguagem;
- rate limit é aplicado por usuário e conversa;
- exclusão lógica respeita a política definida;
- marcação de leitura só pode ser feita pelo destinatário.

#### Testes esperados

- integração de service com banco;
- testes de paginação e filtros;
- relógio controlado para janelas temporais;
- autorização cruzada;
- testes de transição de estado;
- verificação de queries e ausência de N+1 nos fluxos principais.

### Nível 3 — Complexas

Regras com concorrência, transações distribuídas entre agregados, auditoria crítica, cálculo temporal ou comunicação assíncrona.

#### Trust score

- score inicia em `100%`;
- cada alteração registra valor anterior, valor novo, causa, regra e data;
- eventos confirmados aplicam pesos versionados;
- eventos pendentes não causam banimento permanente;
- score recupera dez pontos percentuais após cada período completo de sete dias elegível;
- recuperação nunca ultrapassa `100%`;
- nova infração confirmada reinicia ou altera a elegibilidade conforme fórmula aprovada;
- suspensão pausa a recuperação;
- recuperação não reativa conta suspensa;
- atingir `50%` ou menos suspende imediatamente e abre revisão;
- revisão que confirma `35%` ou menos aplica banimento permanente;
- reversão restaura score e efeitos incorretos sem apagar auditoria;
- atualização concorrente não pode perder penalidade ou recuperação.

#### Moderação transacional

- ação que altera conteúdo, score e sanção é atômica;
- falha parcial faz rollback completo;
- somente transições permitidas são aplicadas;
- toda decisão identifica moderador, evidência e versão da regra;
- banimento encerra acesso HTTP e sessões WebSocket;
- lista de bloqueio é interna, auditável e reversível;
- recurso pode manter, reduzir ou reverter a sanção;
- denúncias coordenadas não devem produzir banimento definitivo sem revisão humana.

#### Filtro de linguagem

- Unicode é normalizado sem corromper o texto original;
- mapa de leetspeak gera candidatos sem explosão combinatória;
- separadores e repetições são tratados sem bloquear substrings legítimas;
- lista de exceções prevalece apenas no contexto previsto;
- regra aplicada é versionada para auditoria;
- tentativas repetidas não transformam a API em ferramenta de descoberta do dicionário;
- detecção automática isolada não causa banimento permanente.

#### Concorrência e antiabuso

- duas criações simultâneas não ultrapassam silenciosamente o limite de cinco posts;
- duas reações simultâneas não criam duplicação;
- troca e remoção concorrentes mantêm contador consistente;
- denúncia duplicada é impedida por regra e constraint;
- recuperação semanal concorrente com penalidade mantém todos os eventos;
- retries de mensagem não geram duplicação quando houver chave idempotente;
- manipulação coordenada de reações ou denúncias pode ser encaminhada para revisão.

#### WebSocket e chat em tempo real

- autenticação da conexão vincula sessão à identidade real;
- autorização é aplicada em cada mensagem, não somente no handshake;
- cliente não assina destino privado de outro usuário;
- persistência e entrega têm semântica clara em caso de falha;
- retry e reconexão não duplicam mensagens confirmadas;
- múltiplas sessões do mesmo usuário recebem eventos conforme política definida;
- bloqueio aplicado durante conexão impede mensagens posteriores;
- suspensão ou banimento invalida sessões abertas;
- ordem de mensagens é preservada ou reconciliada por ID e timestamp;
- broker em memória funciona em uma instância e sua limitação é explícita;
- evolução para broker externo não altera o contrato público.

#### Testes esperados

- testes transacionais e de rollback;
- testes paralelos de concorrência;
- integração com PostgreSQL real para constraints críticas;
- testes WebSocket autenticados;
- simulação de desconexão, retry e usuário offline;
- teste de carga básico;
- testes de auditoria e recalculabilidade do trust score;
- testes de segurança tentando acessar destinos e recursos de terceiros.

### Ordem recomendada dentro dos níveis

1. Implementar e estabilizar as regras básicas de uma funcionalidade.
2. Adicionar suas regras médias antes de abrir o endpoint como completo.
3. Implementar regras complexas atrás de testes de integração e constraints de banco.
4. Não considerar uma funcionalidade pronta apenas porque seu fluxo básico funciona.

Exemplo: reações começam com validação de alvo, avançam para troca e contadores e terminam com concorrência e proteção contra manipulação.

## 6. Estrutura de packages planejada

```text
com.devnest.community
├── controller
│   ├── forum
│   ├── post
│   ├── comment
│   └── interaction
├── dto
│   ├── forum
│   ├── post
│   ├── comment
│   ├── reaction
│   ├── block
│   └── report
├── entity
│   ├── forum
│   ├── post
│   ├── comment
│   ├── reaction
│   ├── moderation
│   └── userrelation
├── mapper
├── repository
└── service
    ├── forum
    ├── post
    ├── comment
    ├── reaction
    ├── moderation
    └── access

com.devnest.chat
├── config
├── controller
├── dto
├── entity
├── repository
├── service
└── websocket
```

Operações administrativas expostas por HTTP ficam em `com.devnest.admin.controller.community`, mas transições de estado continuam implementadas nos services do domínio `community`.

## 7. Fase 0 — contratos e fundação

### Objetivo

Definir contratos compartilhados e preparar o domínio sem expor endpoints incompletos.

### Implementação

- criar enums de status, tipo de post, reação, denúncia e sanção;
- criar propriedades configuráveis da comunidade;
- definir relógio injetável para limites, recuperação semanal e testes determinísticos;
- criar exceptions específicas somente quando as existentes não representarem o caso;
- definir DTOs de paginação ou reutilizar o contrato padrão escolhido pelo projeto;
- definir política de exclusão lógica;
- definir interfaces do sanitizador e filtro de linguagem;
- criar test builders/fixtures para usuários e conteúdo da comunidade.

### Propriedades candidatas

```yaml
devnest:
  community:
    limits:
      posts-per-24-hours: 5
    trust:
      initial-score: 100
      suspension-threshold: 50
      permanent-ban-threshold: 35
      weekly-recovery-points: 10
    content:
      max-post-title-length: 160
      max-post-content-length: 20000
      max-comment-length: 5000
```

Valores sensíveis e configurações específicas do ambiente continuam vindo de ENVs. Regras operacionais podem ter defaults versionados e override por ENV.

### Testes

- carregamento e validação das propriedades;
- transições válidas dos enums/status;
- contratos de sanitização e filtro com implementações fake;
- comportamento do relógio nos limites de tempo.

### Critério de aceite

- aplicação inicia com as propriedades válidas;
- configuração inválida falha com mensagem clara;
- nenhuma regra temporal depende diretamente de `now()` espalhado pelos services.

## 8. Fase 1 — fóruns, posts e feed

### Migration

Criar:

- `community_forums`;
- `community_posts`;
- índices para status, fórum, autor, criação e ordenação do feed;
- constraint única para `forum.slug`;
- foreign keys para usuário, projeto e curso quando os vínculos opcionais entrarem no contrato.

### Entities

- `CommunityForum`;
- `CommunityPost`;
- `CommunityForumStatus`;
- `CommunityPostStatus`;
- `CommunityPostType`.

### Repositories

- `CommunityForumRepository`;
- `CommunityPostRepository`;
- queries paginadas para feed geral e por fórum;
- contagem de posts do autor em janela móvel de 24 horas;
- queries sem carregar coleções de comentários e reações.

### Services

- `CommunityForumService`;
- `CommunityPostService`;
- `CommunityAccessService`;
- validação de fórum ativo;
- criação limitada a cinco posts em 24 horas;
- ownership para edição e exclusão;
- visibilidade compatível com usuário bloqueado ou silenciado;
- exclusão lógica.

### Endpoints

- `GET /community/forums`;
- `GET /community/forums/{slug}`;
- `GET /community/forums/{slug}/posts`;
- `GET /community/posts`;
- `GET /community/posts/{postId}`;
- `POST /community/forums/{forumId}/posts`;
- `PATCH /community/posts/{postId}`;
- `DELETE /community/posts/{postId}`;
- endpoints administrativos de fórum.

### Testes

- criar, consultar, editar e excluir post próprio;
- impedir alteração por outro usuário;
- impedir criação no fórum arquivado;
- aceitar cinco posts e rejeitar o sexto dentro da janela;
- liberar nova criação quando o primeiro post sair da janela;
- feed paginado com ordenação estável;
- exclusão lógica ausente no feed comum;
- feed por fórum sem vazamento entre fóruns;
- validações de título e conteúdo;
- acesso administrativo aos fóruns.

### Critério de aceite

- feed geral e por fórum funcionam com paginação;
- limite de cinco posts é consistente sob requisições concorrentes;
- entidades JPA não são retornadas diretamente;
- queries principais não apresentam N+1.

## 9. Fase 2 — comentários e conteúdo seguro

### Migration

Criar:

- `community_comments`;
- índices por post, autor, status e criação;
- suporte opcional a `parent_comment_id` somente se respostas entrarem nesta entrega.

### Implementação

- `CommunityComment`, DTOs, repository, mapper e service;
- criação, paginação, edição e exclusão lógica;
- sanitização no backend;
- normalização Unicode `NFKC`;
- comparação em minúsculas e sem acentos;
- redução controlada de repetição e separadores;
- mapa de substituição de leetspeak;
- dicionário configurável, versionado e não hardcoded nos services;
- lista de exceções para falsos positivos;
- resposta uniforme para conteúdo bloqueado ou retido.

### Pipeline

```text
Request
  → Bean Validation
  → Sanitização de segurança
  → Cópia normalizada
  → Filtro de linguagem
  → APPROVED | REJECTED | HELD_FOR_REVIEW
  → Persistência, quando permitido
```

### Testes

- comentário em post ativo;
- rejeição em post oculto, removido ou bloqueado para interação;
- ownership de edição/exclusão;
- sanitização de tags e atributos perigosos;
- scripts, eventos HTML e URLs inseguras;
- Unicode, acentos, símbolos, espaçamento e leetspeak;
- mapa `0 → o`, `4 → a`, `@ → a` e casos ambíguos;
- falsos positivos por substring;
- exceções permitidas;
- conteúdo original preservado quando aprovado;
- não registrar conteúdo sensível em logs comuns.

### Critério de aceite

- post e comentário passam pelo mesmo contrato de conteúdo seguro;
- frontend não é a única barreira de validação;
- decisão do filtro é auditável por versão da regra;
- testes cobrem contornos comuns sem depender de IA.

## 10. Fase 3 — reações, bloqueios e anti-spam

### Migration

Criar:

- `community_reactions`;
- `community_user_blocks`;
- `community_user_mutes`;
- constraints únicas para reação por usuário/alvo;
- constraints únicas para bloqueio e silenciamento por par de usuários;
- check constraint garantindo que a reação tenha exatamente um alvo.

### Implementação

- adicionar, trocar e remover reação;
- decidir se `LIKE` é um `ReactionType`;
- contadores agregados por post e comentário;
- bloquear e desbloquear usuário;
- silenciar e dessilenciar usuário;
- filtrar silenciados no feed;
- impedir interação direta e chat entre usuários bloqueados;
- rate limits configuráveis;
- detecção determinística de conteúdo duplicado;
- limite configurável de links conforme decisão do produto.

### Concorrência

- constraint no banco é a fonte final de unicidade;
- operações de reação devem ser idempotentes;
- colisões de inserção são convertidas em resposta consistente;
- contadores não podem depender apenas de incremento em memória;
- testes paralelos validam duas requisições simultâneas do mesmo usuário.

### Testes

- uma reação por usuário e alvo;
- troca sem duplicação;
- remoção idempotente;
- reação própria sem benefício de trust score;
- contadores corretos após troca e remoção;
- bloqueio impedindo interação e início de chat;
- silenciamento removendo conteúdo apenas do feed pessoal;
- bloqueio não revelando dados privados;
- rate limits e conteúdo duplicado;
- concorrência nas reações.

### Critério de aceite

- não existem contadores inflados por duplicação;
- bloqueio e silenciamento possuem semânticas distintas;
- restrições são aplicadas no service, não somente no controller.

## 11. Fase 4 — denúncias, trust score e moderação

### Migration

Criar:

- `community_reports`;
- `community_moderation_cases`;
- `community_moderation_actions`;
- `community_trust_events`;
- `community_appeals`;
- `community_blocklist`;
- coluna ou entidade de trust score associada ao usuário;
- índices para filas por status e criação;
- unicidade de denúncia por denunciante e alvo.

Preferência: armazenar o score atual para leitura rápida e manter todos os eventos necessários para recalculá-lo e auditá-lo.

### Implementação

- denunciar post ou comentário;
- impedir denúncia duplicada;
- listar fila administrativa com filtros;
- descartar ou confirmar denúncia;
- ocultar, restaurar e remover conteúdo;
- aplicar aviso, restrição, suspensão e banimento;
- criar caso automático ao atingir `50%`;
- impedir reativação apenas pela recuperação semanal;
- banir permanentemente após revisão em `35%` ou menos;
- registrar lista interna de bloqueio;
- criar e avaliar recurso;
- recuperar `10` pontos a cada período completo de sete dias elegível;
- pausar recuperação durante suspensão;
- penalizar abuso confirmado de denúncias.

### Consistência transacional

Uma decisão de moderação que altera conteúdo, score e sanção deve ocorrer na mesma transação. Falha parcial não pode ocultar o conteúdo sem registrar a ação, nem reduzir o score sem evento de auditoria.

### Testes

- denúncia válida e duplicada;
- denúncia descartada sem penalidade;
- denúncia confirmada com peso configurado;
- abuso de denúncias;
- trilha completa de alteração do score;
- recuperação `50 → 60`, `60 → 70` e limite em `100`;
- ausência de recuperação antes de sete dias completos;
- suspensão imediata em `50`;
- recuperação pausada durante suspensão;
- revisão e banimento confirmado em `35`;
- inclusão e reversão na lista de bloqueio;
- recurso avaliado por usuário autorizado;
- concorrência entre recuperação e nova penalidade;
- autorização de todos os endpoints administrativos.

### Critério de aceite

- qualquer alteração do score explica valor anterior, valor novo e causa;
- nenhuma denúncia não revisada causa banimento permanente;
- banimento e reversão são auditáveis;
- operações administrativas são protegidas por `ADMIN` ou futura role aprovada;
- lista de bloqueio não é exposta em APIs comuns.

## 12. Fase 5 — chat privado em tempo real

### Dependência

Adicionar `spring-boot-starter-websocket` ao Maven. Começar com o broker simples do Spring em uma única instância.

### Migration

Criar:

- `chat_conversations`;
- `chat_conversation_participants` ou duas referências normalizadas por conversa;
- `chat_messages`;
- índices para conversa, criação, destinatário e mensagens não lidas;
- constraint que impeça duas conversas para o mesmo par de usuários.

### Configuração WebSocket

- endpoint de handshake `/ws`;
- prefixo de aplicação `/app`;
- destino privado `/user/queue/messages`;
- autenticação JWT no handshake ou frame STOMP `CONNECT`;
- `ChannelInterceptor` para identidade e autorização;
- configuração explícita de origens permitidas;
- limites de frame, mensagem, buffer e inatividade;
- eventos de conexão e desconexão somente quando necessários.

### REST

- iniciar ou obter conversa privada;
- listar conversas do usuário;
- carregar histórico paginado;
- marcar mensagens como lidas;
- excluir logicamente mensagem própria;
- denunciar mensagem, caso aprovado no contrato final.

### WebSocket

- enviar mensagem em `/app/chat.send`;
- receber mensagem em `/user/queue/messages`;
- confirmar persistência com ID definitivo;
- entregar mensagens a sessões conectadas do destinatário;
- não perder histórico quando o destinatário estiver offline;
- nunca aceitar `senderId` do payload como fonte de verdade.

### Regras

- somente participantes acessam conversa e histórico;
- usuário bloqueado não inicia conversa nem envia mensagem;
- sanitização e filtro de linguagem antes da persistência;
- rate limit por usuário, conversa e conexão;
- exclusão lógica preserva evidência de denúncia;
- ordenação usa timestamp e ID estáveis;
- mensagens duplicadas por retry devem usar identificador idempotente do cliente, se adotado.

### Testes

- handshake autenticado e não autenticado;
- JWT inválido e expirado;
- subscription a destino privado;
- tentativa de acessar destino de outro usuário;
- envio entre participantes;
- envio bloqueado por relação de bloqueio;
- mensagem sanitizada e rejeitada pelo filtro;
- persistência antes da confirmação;
- usuário online e offline;
- histórico paginado e ordenado;
- mensagens não lidas e leitura;
- rate limit;
- reconexão e retry sem duplicação, se houver chave idempotente;
- isolamento entre duas conversas simultâneas.

### Critério de aceite

- mensagem entregue em tempo real quando o destinatário está conectado;
- mensagem disponível no histórico quando ele está desconectado;
- nenhum usuário consegue assinar, ler ou enviar como outra identidade;
- falha do WebSocket não remove mensagens já persistidas;
- testes de integração cobrem REST e WebSocket.

## 13. Fase 6 — integração, performance e documentação

### Integração

- revisar regras cruzadas de bloqueio em feed, comentários, reações e chat;
- revisar efeitos de suspensão e banimento em todos os endpoints;
- garantir que usuário banido perca também sessões WebSocket;
- revisar exclusão de usuário e retenção de conteúdo;
- validar CORS HTTP e origens WebSocket;
- padronizar `ApiError` e erros de mensagens.

### Performance

- analisar planos das queries de feed e histórico;
- validar índices com volume representativo;
- eliminar N+1;
- usar projeções ou queries agregadas para contadores;
- testar paginação profunda e considerar cursor quando necessário;
- executar teste de carga básico em feed, reações e chat;
- documentar limite do broker em memória.

### Documentação

- atualizar `README.md`;
- atualizar `docs/ARCHITECTURE.md`;
- mover rotas implementadas para `docs/API.md`;
- manter propostas futuras em `docs/COMMUNITY.md`;
- documentar novas ENVs e propriedades;
- adicionar exemplos REST e WebSocket/STOMP.

### Critério de aceite

- suíte completa passa em ambiente limpo;
- migrations sobem do zero e sobre uma base na versão anterior;
- documentação diferencia implementado de backlog;
- nenhuma configuração secreta é hardcoded;
- logs não expõem JWT, conteúdo privado ou dados da lista de bloqueio.

## 14. Estratégia de testes

### Testes unitários

- normalização e filtro de linguagem;
- cálculo e recuperação do trust score;
- transições de status;
- políticas de acesso;
- rate limits e janelas temporais;
- regras de sanção.

### Testes de service

- regras com repositories reais em H2 quando compatíveis;
- ownership e roles;
- fluxos transacionais;
- concorrência em reações, posts e trust score;
- bloqueio cruzado entre comunidade e chat.

### Testes de controller

- validação dos requests;
- status HTTP;
- autorização por role;
- formato de `ApiError`;
- paginação e filtros.

### Testes WebSocket

- conexão e autenticação;
- envio e subscription;
- destinos privados;
- desconexão e reconexão;
- autorização por mensagem;
- entrega online e persistência offline.

### Testes de migration

H2 não substitui integralmente PostgreSQL para constraints, índices e concorrência. Antes de produção, executar migrations e fluxos críticos contra PostgreSQL real, preferencialmente com ambiente descartável de integração.

## 15. Ordem sugerida de migrations

Os números definitivos dependem da última migration existente no momento da implementação.

```text
V7__create_community_forums_and_posts.sql
V8__create_community_comments.sql
V9__create_community_reactions.sql
V10__create_community_user_relations.sql
V11__create_community_rate_limit_events.sql
V12__create_community_reports.sql
V13__create_community_moderation.sql
V14__create_chat.sql
V15__add_community_performance_indexes.sql
```

Evitar editar migrations já aplicadas. Ajustes posteriores recebem nova versão.

## 16. Pull requests ou checkpoints sugeridos

1. `community-foundation-and-forums`
2. `community-posts-feed-and-comments`
3. `community-content-safety`
4. `community-reactions-blocks-and-rate-limits`
5. `community-reports-and-moderation`
6. `community-trust-score-and-appeals`
7. `private-chat-persistence-and-rest`
8. `private-chat-websocket`
9. `community-integration-performance-docs`

Cada checkpoint deve ser utilizável, revisável e possuir testes próprios. Evitar um único PR com todo o domínio.

## 17. Definição de pronto

Uma fase está pronta quando:

- requisitos e decisões daquela fase estão definidos;
- migration foi criada e validada;
- entidades não vazam pela API;
- regras ficam nos services;
- repositories possuem apenas responsabilidades de persistência;
- autorização por role e recurso foi testada;
- exceptions usam o tratamento global;
- casos felizes, limites e falhas possuem testes;
- concorrência foi avaliada onde existe unicidade ou contador;
- configuração não contém segredo hardcoded;
- documentação foi atualizada;
- `mvn test` passa.

## 18. Backlog pós-MVP

- medalhas e conquistas, incluindo `Java Winner`;
- trends determinísticas;
- desafios integrados a projetos;
- notificações gerais;
- respostas aninhadas;
- solução aceita em perguntas;
- anexos e imagens;
- busca textual avançada;
- feed personalizado;
- broker externo e escala horizontal do chat;
- presença e indicador de digitação, se não entrarem no MVP.
