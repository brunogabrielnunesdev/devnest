# Comunidade DevNest

> Documento vivo de produto. Fóruns, posts, comentários, reações, bloqueios, silenciamentos, proteções anti-spam e denúncias com fila administrativa já estão implementados; trust score, moderação avançada e chat continuam planejados.

Plano técnico relacionado: [COMMUNITY_EXECUTION_PLAN.md](COMMUNITY_EXECUTION_PLAN.md).

Modelagem do primeiro incremento: [COMMUNITY_MODEL.md](COMMUNITY_MODEL.md).

## Visão do produto

A comunidade deve ser o espaço em que desenvolvedores compartilham conhecimento, mostram projetos, tiram dúvidas e encontram pessoas com interesses semelhantes. Ela conecta os dois domínios existentes do DevNest: aprendizado e projetos.

## Objetivos

- permitir discussões técnicas úteis e pesquisáveis;
- aproximar alunos, professores e criadores de projetos;
- dar visibilidade aos projetos desenvolvidos na plataforma;
- estimular colaboração sem transformar a comunidade em uma rede social genérica;
- oferecer ferramentas suficientes de moderação e segurança desde o MVP.

## Fora do escopo inicial

- chamadas de áudio ou vídeo;
- grupos privados complexos;
- marketplace ou pagamentos;
- algoritmo avançado de recomendação;
- chats públicos e conversas em grupo.

## Planejamento funcional

## Escopo básico confirmado

O núcleo inicial da comunidade está confirmado com estas funcionalidades:

- feed de publicações;
- criação e visualização de posts;
- comentários em posts;
- curtidas;
- reações;
- denúncias de posts e comentários;
- moderação humana, sem uso de inteligência artificial;
- bloqueio e silenciamento entre usuários;
- proteção contra spam e manipulação de interações;
- auditoria, recurso e sanções progressivas;
- exclusão lógica de conteúdo.

Funcionalidades além desse núcleo continuam como propostas e só entram no escopo após decisão explícita.

### Publicações

Usuários autenticados podem criar publicações com:

- título;
- conteúdo em texto ou Markdown seguro;
- tipo da publicação;
- tags;
- projeto relacionado opcional;
- curso relacionado opcional.

Tipos iniciais sugeridos:

- `DISCUSSION`: discussão geral;
- `QUESTION`: dúvida técnica;
- `PROJECT_SHOWCASE`: apresentação de projeto;
- `RESOURCE`: material ou recurso útil.

### Interações

- listar e visualizar publicações;
- comentar em uma publicação;
- curtir publicações e comentários;
- reagir a publicações e comentários;
- editar ou excluir o próprio conteúdo;

Respostas aninhadas e solução de perguntas ainda não fazem parte do escopo confirmado.

### Bloquear e silenciar usuários

- bloquear impede novas interações diretas entre as contas;
- silenciar remove posts e comentários daquela conta do feed do usuário que aplicou a ação;
- o usuário bloqueado ou silenciado não recebe acesso ao motivo nem a dados privados de quem realizou a ação;
- bloqueio e silenciamento são preferências pessoais e não alteram diretamente o trust score;
- administradores continuam capazes de consultar conteúdo relevante durante uma moderação.

### Descoberta

- feed recente;
- ordenação por relevância, atividade e mais curtidos;
- filtro por tipo e tags;
- busca por título e conteúdo;
- página de publicações de um usuário;
- vínculo visível com curso ou projeto quando existir.

### Fóruns temáticos — proposta

Fóruns como “Java”, “Spring” ou “React” podem reutilizar integralmente posts, comentários, curtidas, reações, denúncias e moderação. O fórum funciona como um contêiner temático, não como um segundo sistema de publicação.

Modelo sugerido:

`CommunityForum`:

- `id: UUID`;
- `name: String`;
- `slug: String` único;
- `description: String`;
- `status: ACTIVE, ARCHIVED`;
- `createdById: UUID`;
- `createdAt: OffsetDateTime`;
- `updatedAt: OffsetDateTime`.

`CommunityPost` recebe `forumId: UUID`. Comentários e demais interações continuam ligados ao post e, portanto, não precisam armazenar `forumId` novamente.

Relação principal:

```text
CommunityForum
    └── CommunityPost
            ├── CommunityComment
            ├── CommunityReaction
            └── CommunityReport
```

Regras sugeridas:

- cada post pertence a exatamente um fórum;
- somente administradores criam, editam, arquivam ou removem fóruns no MVP;
- arquivar um fórum impede novos posts, mas preserva o conteúdo existente;
- o feed geral agrega posts de todos os fóruns acessíveis;
- a página do fórum reutiliza o mesmo feed com filtro por `forumId` ou `slug`;
- trust score, filtro de linguagem, bloqueios e sanções são globais, não reiniciados por fórum;
- tags continuam opcionais e descrevem assuntos mais específicos dentro do fórum;
- fóruns devem usar slug estável em URLs e ID nas relações internas.

API candidata:

| Método | Endpoint | Descrição |
|---|---|---|
| GET | `/community/forums` | Listar fóruns ativos |
| GET | `/community/forums/{slug}` | Consultar fórum |
| GET | `/community/forums/{slug}/posts` | Feed paginado do fórum |
| POST | `/community/forums/{forumId}/posts` | Criar post no fórum |
| POST | `/admin/community/forums` | Criar fórum |
| PATCH | `/admin/community/forums/{forumId}` | Editar ou arquivar fórum |

Essa estrutura permite adicionar fóruns sem duplicar services de comentários, reações ou denúncias. Apenas criação, administração e consulta de fóruns exigem componentes novos.

### Moderação

- a moderação será baseada em regras e ações humanas, sem análise por IA;
- status de publicação e comentário;
- denúncias com motivo e descrição opcional;
- fila administrativa de denúncias;
- ocultação, restauração e remoção de conteúdo;
- registro do moderador, motivo e data da decisão;
- bloqueio de novas interações em uma publicação.

### Fluxo de moderação confirmado

1. Um usuário denuncia um post ou comentário e seleciona um motivo.
2. A denúncia entra em uma fila administrativa como `PENDING`.
3. Um administrador consulta o conteúdo, o contexto e denúncias anteriores.
4. O administrador descarta a denúncia ou aplica uma ação ao conteúdo.
5. A decisão registra moderador, data, motivo e observação opcional.

Regras automáticas simples podem complementar a moderação humana sem IA, por exemplo:

- limite máximo de `5` posts por usuário em uma janela móvel de `24 horas`;
- rate limits configuráveis para comentários e reações; o limite de denúncias ainda será definido;
- bloqueio implementado de posts e comentários duplicados do mesmo autor em janela configurável;
- limite de denúncias criadas pelo mesmo usuário;
- ocultação preventiva após um limite configurável de denúncias distintas;
- rejeição de conteúdo que exceda os limites de tamanho;
- bloqueio de URLs ou padrões explicitamente proibidos.

A ocultação automática por quantidade de denúncias ainda precisa ser decidida. Ela deve ser opcional, pois denúncias coordenadas podem ser usadas para silenciar conteúdo legítimo.

### Proteções anti-spam implementadas

- cinco posts por usuário em 24 horas, inclusive sob requisições concorrentes;
- rate limit persistido para criação de comentários e alteração de reações;
- detecção de duplicidade com normalização de Unicode, caixa, acentos, pontuação e espaços;
- bloqueio pessimista por usuário nas operações sensíveis;
- validação dos cenários concorrentes em PostgreSQL 16.

### Sanções progressivas

As ações de moderação confirmadas seguem uma escala proporcional:

1. aviso formal;
2. bloqueio temporário para criar posts ou comentários;
3. suspensão temporária da conta;
4. suspensão automática para revisão ao atingir `50%` de trust score;
5. banimento permanente após revisão quando o score confirmado for `35%` ou menos.

O histórico, a gravidade e a reincidência podem permitir avançar etapas. Toda sanção deve possuir motivo, duração quando temporária, evidências e responsável.

### Auditoria e recurso

- alterações no trust score e ações de moderação geram registros imutáveis de auditoria;
- o registro informa regra ou moderador, motivo, evidência, valores anteriores e novos e data;
- o usuário pode contestar sanções e banimentos;
- quando possível, o recurso deve ser analisado por moderador diferente daquele que tomou a decisão original;
- a revisão pode manter, reduzir ou reverter a sanção;
- reversões não apagam o histórico, mas registram a correção e restauram os efeitos indevidos.

### Exclusão lógica

- posts e comentários excluídos pelo autor ou pela moderação permanecem armazenados com status apropriado;
- conteúdo excluído deixa de aparecer normalmente no feed e não aceita novas interações;
- a API pública retorna apenas a indicação de que o conteúdo foi removido quando isso for necessário para preservar a conversa;
- o conteúdo original fica disponível somente para auditoria e moderação autorizada;
- a política de retenção e anonimização ainda precisa ser definida.

### Trust score e bloqueio de conta

Cada usuário da comunidade terá um `trust score`, iniciado em `100%`, que representa a confiança operacional da conta.

O score continuará representado por seu percentual exato. A proposta de substituí-lo por estados abstratos como “boa reputação” ou “atenção” foi descartada. Ainda precisa ser decidido quem poderá visualizar esse percentual.

Regra de bloqueio definida:

1. Enquanto o score estiver acima de `50%`, a conta permanece ativa, sujeita às restrições normais.
2. Quando o score cair para `50%` ou menos, a conta será suspensa automaticamente e de forma imediata.
3. A suspensão cria um caso obrigatório de revisão manual; ela não resulta diretamente em banimento permanente.
4. Um moderador analisa as denúncias, o histórico e as evidências que causaram a redução.
5. Se, após a revisão humana, o score confirmado ficar em `35%` ou menos, a conta perde permanentemente o acesso à plataforma.
6. A conta banida é registrada em uma lista interna de bloqueio.
7. Se a revisão não confirmar o limite de banimento permanente, o moderador registra a decisão, ajusta o score quando necessário e decide entre reativar a conta ou aplicar uma sanção temporária.

A lista de bloqueio não será pública. Cada registro deve conter:

- usuário bloqueado;
- motivo padronizado;
- evidências ou referências às denúncias consideradas;
- score antes e depois da revisão;
- moderador responsável;
- data da decisão;
- observação administrativa;
- status de eventual recurso;
- data de reversão, quando aplicável.

O identificador usado para impedir novo acesso ainda precisa ser definido. E-mail pode ser armazenado de maneira normalizada, mas dados adicionais, IPs ou fingerprints exigem avaliação de privacidade, retenção e falsos positivos.

### Palavras inadequadas — em discussão

O sistema poderá identificar palavras e expressões proibidas sem utilizar IA. A detecção pode usar uma lista configurável e normalização do texto, mas a consequência ainda não está definida.

Pontos que precisam ser considerados:

- palavras podem ter significado diferente dependendo do contexto;
- termos técnicos, citações e discussões educativas podem gerar falsos positivos;
- usuários podem tentar contornar o filtro com espaços, símbolos, números ou alterações ortográficas;
- bloquear qualquer ocorrência pode impedir palavras legítimas que apenas contenham o mesmo trecho;
- idiomas e variações regionais exigem listas diferentes;
- a lista de termos não deve ficar hardcoded no código da aplicação;
- mudanças na lista e nas penalidades precisam ser auditáveis.

Fluxos possíveis:

1. **Aviso:** informa o termo detectado e permite que o usuário edite antes de publicar.
2. **Bloqueio da publicação:** rejeita o conteúdo, mas não reduz o trust score na primeira ocorrência.
3. **Publicação retida:** não exibe o conteúdo e o envia para revisão manual.
4. **Penalidade progressiva:** ocorrências confirmadas reduzem o trust score, com peso maior para reincidência.

Recomendação inicial: avisar e bloquear o envio em ocorrências claras; encaminhar casos ambíguos ou reincidentes para revisão. A detecção automática, sozinha, não deve causar banimento permanente.

#### Pipeline proposto para posts e comentários

Ao enviar um post ou comentário:

1. A API valida tipo, campos obrigatórios e limites de tamanho.
2. O conteúdo passa pela sanitização de segurança.
3. Uma cópia do texto é normalizada exclusivamente para análise.
4. O filtro compara a cópia normalizada com a lista configurável de termos e expressões.
5. A API retorna `aprovado`, `bloqueado` ou `retido para revisão`.
6. Somente conteúdo aprovado é persistido e publicado imediatamente.

Sanitização e filtro de linguagem são responsabilidades diferentes:

- a sanitização protege contra HTML, scripts, URLs e payloads perigosos;
- o filtro de linguagem aplica as regras de convivência;
- um conteúdo pode ser tecnicamente seguro e ainda violar as regras da comunidade;
- o backend deve executar as duas etapas mesmo que o frontend também faça validações.

#### Normalização para evitar contorno

O filtro pode montar uma representação normalizada sem modificar o conteúdo original do usuário:

- normalização Unicode, preferencialmente `NFKC`;
- conversão para minúsculas;
- remoção controlada de acentos para comparação;
- redução de letras repetidas em excesso;
- tratamento de espaços, pontos, hífens e símbolos inseridos entre letras;
- mapa de caracteres visualmente semelhantes, incluindo leetspeak.

Exemplo inicial de mapa de análise:

```text
0 → o
1 → i ou l
3 → e
4 → a
5 → s
7 → t
@ → a
$ → s
```

Mapeamentos ambíguos, como `1 → i ou l`, podem gerar mais de uma forma candidata para comparação. O mapa não deve substituir o texto armazenado nem aparecer como correção automática para o usuário.

Exemplo conceitual:

```text
Entrada original      → "p4.l@v-r4"
Cópia normalizada     → "palavra"
Texto armazenado      → permanece original, caso seja aprovado
```

#### Cuidados do filtro

- comparar palavras completas e expressões, evitando bloquear termos que apenas contenham o mesmo trecho;
- manter lista de exceções permitidas para falsos positivos conhecidos;
- versionar a lista, o mapa e as regras usadas na decisão;
- retornar um código de motivo sem necessariamente revelar toda a lista proibida;
- não registrar conteúdo sensível em logs comuns;
- limitar tentativas repetidas para impedir que a API seja usada para descobrir o dicionário;
- testar português, inglês, acentos, Unicode e formas comuns de leetspeak;
- manter a decisão automática reversível e auditável.

Resposta candidata quando o conteúdo for bloqueado:

```json
{
  "status": 422,
  "error": "CommunityContentRejected",
  "message": "O conteúdo contém linguagem não permitida.",
  "reason": "PROHIBITED_LANGUAGE"
}
```

O status HTTP e o nível de detalhe da resposta ainda podem ser ajustados durante a definição do contrato da API.

### Requisitos do cálculo de confiança

- o score recupera `10` pontos percentuais a cada período completo de sete dias sem nova infração confirmada;
- exemplos de recuperação: `50% → 60%`, `60% → 70%` e `90% → 100%`;
- o score nunca ultrapassa `100%`;
- a recuperação é calculada pelo tempo decorrido, e não por um job que dependa de executar exatamente em determinado dia da semana;
- denúncias pendentes, isoladamente, não interrompem a recuperação; somente uma infração confirmada produz esse efeito;
- quando uma conta chega a `50%` ou menos e é suspensa, sua recuperação automática fica pausada até a conclusão da revisão manual;
- a recuperação do score não reativa automaticamente uma conta suspensa;
- contas permanentemente banidas não recuperam score;
- nenhuma denúncia isolada reduz o score sem uma regra previamente definida;
- denúncias repetidas do mesmo usuário contra o mesmo conteúdo não acumulam peso;
- denúncias descartadas não reduzem o score e podem restaurar pontos provisoriamente removidos;
- ações confirmadas por moderador possuem peso maior que denúncias ainda pendentes;
- toda alteração registra valor anterior, valor novo, causa e data;
- o cálculo deve ser determinístico, versionado e auditável;
- administradores não alteram o score silenciosamente;
- o usuário deve ter um mecanismo de recurso contra banimento permanente;
- pesos e fórmula ainda precisam ser definidos antes da implementação.

## Análise de viabilidade

### Viabilidade do núcleo

O escopo confirmado é tecnicamente viável dentro da arquitetura atual. O backend já possui elementos reutilizáveis:

- identidade e autenticação JWT;
- autorização com `@PreAuthorize`;
- ownership validado nos services;
- padrão de entidades, DTOs, repositories e tratamento global de erros;
- experiência de comentários e moderação no domínio de cursos;
- operações administrativas protegidas por `ADMIN`;
- PostgreSQL, Flyway e suporte a paginação com Spring Data.

### Complexidade estimada por recurso

| Recurso | Complexidade | Principal cuidado |
|---|---|---|
| Feed cronológico | Baixa | Paginação e ordenação estável |
| Posts | Baixa | Validação, ownership e sanitização |
| Comentários | Baixa a média | Paginação e eventual hierarquia |
| Curtidas | Média | Unicidade por usuário e concorrência |
| Reações | Média | Troca de reação e contadores eficientes |
| Denúncias | Média | Evitar duplicação e abuso do sistema |
| Fila administrativa | Média | Filtros, contexto e auditoria |
| Moderação manual | Média | Transições de status consistentes |
| Trust score | Alta | Fórmula justa, auditoria e abuso coordenado |
| Suspensão e lista de bloqueio | Alta | Recursos, privacidade e reversibilidade |

### Riscos conhecidos

- feed sem paginação pode degradar rapidamente;
- contadores calculados carregando coleções JPA causam problemas de performance;
- curtidas e reações concorrentes exigem constraints no banco;
- exclusão física dificulta auditoria e análise de denúncias;
- permitir HTML sem sanitização cria risco de XSS no frontend;
- moderação exclusivamente reativa pode ficar sobrecarregada com o crescimento;
- regras automáticas muito agressivas podem gerar falsos positivos, mesmo sem IA.
- denúncias coordenadas podem reduzir artificialmente o trust score;
- uma lista de bloqueio baseada somente em e-mail é fácil de contornar;
- bloqueios por IP ou dispositivo podem atingir pessoas diferentes que compartilham a mesma rede ou aparelho.

### Conclusão de viabilidade

O MVP é viável sem IA. A implementação mais segura é incremental: primeiro feed, posts, comentários e interações; depois denúncias e painel administrativo antes da abertura para uma base maior de usuários. A modelagem deve incluir status e auditoria desde o início para evitar uma migração difícil quando a moderação entrar em operação.

## Modelo de domínio inicial

### `CommunityPost`

- `id: UUID`
- `authorId: UUID`
- `title: String`
- `content: String`
- `type: CommunityPostType`
- `status: CommunityContentStatus`
- `projectId: UUID?`
- `courseId: UUID?`
- `solvedCommentId: UUID?`
- `commentsLocked: boolean`
- `createdAt: OffsetDateTime`
- `updatedAt: OffsetDateTime`

### `CommunityComment`

- `id: UUID`
- `postId: UUID`
- `authorId: UUID`
- `parentCommentId: UUID?`
- `content: String`
- `status: CommunityContentStatus`
- `createdAt: OffsetDateTime`
- `updatedAt: OffsetDateTime`

### `CommunityTag`

- `id: UUID`
- `name: String`
- `slug: String`

A relação entre publicações e tags é muitos-para-muitos.

### `CommunityReaction`

- `id: UUID`
- `userId: UUID`
- `postId: UUID?`
- `commentId: UUID?`
- `type: ReactionType`
- `createdAt: OffsetDateTime`

Curtida e reação são conceitos do núcleo confirmado. Ainda precisamos decidir se `LIKE` será um tipo de reação ou um recurso separado. Deve existir no máximo uma interação de cada tipo do mesmo usuário por alvo.

Regras confirmadas contra manipulação:

- uma conta mantém no máximo uma reação ativa por alvo;
- a troca de reação atualiza a interação existente em vez de criar outra;
- constraints no banco garantem a unicidade mesmo com requisições concorrentes;
- reagir ao próprio conteúdo não gera benefício no trust score;
- remover uma reação desfaz qualquer efeito agregado relacionado a ela;
- atividade artificial ou coordenada pode ser sinalizada para revisão;
- contadores devem ser recalculáveis a partir das interações persistidas.

### `CommunityReport`

- `id: UUID`
- `reporterId: UUID`
- `postId: UUID?`
- `commentId: UUID?`
- `reason: ReportReason`
- `description: String?`
- `status: ReportStatus`
- `reviewedById: UUID?`
- `reviewNote: String?`
- `reviewedAt: OffsetDateTime?`
- `createdAt: OffsetDateTime`

### Enums sugeridos

```text
CommunityPostType: DISCUSSION, QUESTION, PROJECT_SHOWCASE, RESOURCE
CommunityContentStatus: ACTIVE, HIDDEN, REMOVED
ReactionType: LIKE
ReportReason: SPAM, HARASSMENT, HATE_SPEECH, MISINFORMATION, INAPPROPRIATE, OTHER
ReportStatus: PENDING, REVIEWED, DISMISSED, ACTION_TAKEN
```

## Regras de negócio iniciais

- somente usuários autenticados criam publicações, comentários, reações e denúncias;
- cada usuário pode criar no máximo `5` posts em qualquer janela móvel de `24 horas`;
- o autor pode editar ou excluir logicamente o próprio conteúdo;
- administradores podem moderar qualquer conteúdo;
- conteúdo oculto ou removido não aceita novas interações;
- somente o autor de uma pergunta pode marcar ou desmarcar a solução;
- a solução deve ser um comentário ativo da própria publicação;
- um projeto só pode ser vinculado quando estiver visível para o autor;
- um curso só pode ser vinculado quando estiver publicado ou pertencer ao professor autor;
- tags são normalizadas por `slug` e não devem ser duplicadas;
- exclusões devem preservar dados mínimos necessários para auditoria e moderação;
- denúncias duplicadas do mesmo usuário para o mesmo alvo não são permitidas;
- abuso confirmado do sistema de denúncias pode gerar sanção progressiva e redução do trust score;
- contadores exibidos pela API não devem depender de carregar coleções JPA completas.

## Autorização

As roles globais existentes continuam válidas:

- `STUDENT`, `TEACHER` e `ADMIN` podem participar da comunidade;
- `ADMIN` possui acesso à fila e às ações de moderação;
- professores não recebem poder de moderação global automaticamente;
- ownership do conteúdo é validado no service, não apenas no controller.

Uma futura role específica, como `MODERATOR`, deve ser avaliada separadamente e não precisa entrar no MVP.

## API candidata

As rotas abaixo são uma proposta e ainda podem mudar.

### Publicações

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/community/posts` | Criar publicação |
| GET | `/community/posts` | Listar, filtrar e paginar |
| GET | `/community/posts/{postId}` | Consultar publicação |
| PATCH | `/community/posts/{postId}` | Editar publicação própria |
| DELETE | `/community/posts/{postId}` | Remover publicação própria |
| POST | `/community/posts/{postId}/solution/{commentId}` | Marcar solução |
| DELETE | `/community/posts/{postId}/solution` | Desmarcar solução |

### Comentários

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/community/posts/{postId}/comments` | Comentar ou responder |
| GET | `/community/posts/{postId}/comments` | Listar comentários |
| PATCH | `/community/comments/{commentId}` | Editar comentário próprio |
| DELETE | `/community/comments/{commentId}` | Remover comentário próprio |

### Reações e denúncias

| Método | Endpoint | Descrição |
|---|---|---|
| PUT | `/community/posts/{postId}/reaction` | Adicionar ou substituir reação |
| DELETE | `/community/posts/{postId}/reaction` | Remover reação |
| PUT | `/community/comments/{commentId}/reaction` | Adicionar ou substituir reação |
| DELETE | `/community/comments/{commentId}/reaction` | Remover reação |
| POST | `/community/posts/{postId}/reports` | Denunciar publicação |
| POST | `/community/comments/{commentId}/reports` | Denunciar comentário |

### Administração

| Método | Endpoint | Descrição |
|---|---|---|
| GET | `/admin/community/reports` | Listar denúncias |
| PATCH | `/admin/community/reports/{reportId}` | Registrar decisão |
| PATCH | `/admin/community/posts/{postId}/status` | Moderar publicação |
| PATCH | `/admin/community/comments/{commentId}/status` | Moderar comentário |

## Organização de packages sugerida

```text
com.devnest.community
├── controller
│   ├── post
│   ├── comment
│   └── interaction
├── dto
├── entity
│   ├── post
│   ├── comment
│   ├── reaction
│   ├── report
│   └── tag
├── mapper
├── repository
└── service
    ├── post
    ├── comment
    ├── interaction
    └── access
```

A moderação administrativa pode ficar em `com.devnest.admin`, consumindo repositories e services públicos do domínio `community`, sem duplicar as regras de transição de status.

## Paginação e performance

- usar paginação desde o primeiro endpoint de listagem;
- definir ordenação estável com `createdAt` e `id`;
- evitar N+1 ao carregar autor, tags e contadores;
- obter quantidade de comentários e reações por queries agregadas;
- considerar índices para `created_at`, `author_id`, `type`, `status` e chaves estrangeiras;
- busca textual pode começar com recursos do PostgreSQL e evoluir sem alterar o contrato público.

## Segurança e qualidade do conteúdo

- limitar tamanho de título, conteúdo e descrição de denúncia;
- sanitizar Markdown ou HTML antes da renderização no frontend;
- não aceitar URLs ou embeds inseguros;
- limitar posts a `5` por usuário em uma janela móvel de `24 horas`;
- aplicar rate limiting também em comentários, reações e denúncias, com valores ainda a definir;
- impedir que denúncias exponham a identidade do denunciante a usuários comuns;
- registrar ações administrativas para auditoria;
- aplicar exclusão lógica ao conteúdo removido pelo autor ou pela moderação.

### Proteção contra links — em discussão

O sistema precisa sanitizar URLs e aceitar somente protocolos seguros. Ainda será decidido se haverá limite de links por post, bloqueio de encurtadores, lista de domínios proibidos ou retenção de posts com links para revisão.

## Chat privado — confirmado

O sistema terá conversas privadas em tempo real entre dois usuários. O chat fará parte da experiência da comunidade, mas será implementado como domínio separado, por exemplo `com.devnest.chat`, para não misturar mensagens com posts e comentários.

O chat será exclusivamente textual. Chamadas de áudio, chamadas de vídeo, salas de voz e transmissão de mídia ao vivo não fazem parte do produto planejado.

O transporte em tempo real usará WebSocket com o suporte do Spring. A abordagem inicial recomendada é STOMP sobre WebSocket, pois fornece destinos, subscriptions, controllers de mensagens e destinos individuais por usuário sem exigir um protocolo de aplicação totalmente próprio.

O projeto precisará adicionar o módulo `spring-boot-starter-websocket`; o starter MVC atual, isoladamente, não representa toda a configuração necessária para o chat.

### Fluxo inicial

1. O cliente autenticado abre uma conexão no endpoint WebSocket.
2. O JWT é validado no handshake ou no frame de conexão, conforme a estratégia definida.
3. O cliente se inscreve em um destino privado associado à própria identidade autenticada.
4. Para enviar uma mensagem, informa o destinatário e o conteúdo em um destino de aplicação.
5. O backend valida bloqueios, rate limit, tamanho, sanitização e filtro de linguagem.
6. A mensagem é persistida antes da confirmação de envio.
7. O servidor entrega a mensagem ao destino privado do destinatário quando ele estiver conectado.
8. O histórico continua disponível por API REST, inclusive quando o destinatário estava offline.

### Modelo inicial

`Conversation`:

- `id: UUID`;
- dois participantes únicos;
- `createdAt` e `updatedAt`;
- referência à última mensagem para ordenação eficiente.

`ChatMessage`:

- `id: UUID`;
- `conversationId: UUID`;
- `senderId: UUID`;
- `content: String`;
- `status: SENT, DELIVERED, READ`;
- `createdAt: OffsetDateTime`;
- `editedAt: OffsetDateTime?`;
- `deletedAt: OffsetDateTime?`.

Uma conversa entre o mesmo par de usuários deve ser única, independentemente de quem a iniciou.

### Contratos candidatos

REST:

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/chat/conversations` | Iniciar ou obter conversa privada |
| GET | `/chat/conversations` | Listar conversas do usuário |
| GET | `/chat/conversations/{conversationId}/messages` | Consultar histórico paginado |
| PATCH | `/chat/conversations/{conversationId}/read` | Marcar mensagens como lidas |
| DELETE | `/chat/messages/{messageId}` | Excluir logicamente mensagem própria |

WebSocket/STOMP:

| Destino | Direção | Descrição |
|---|---|---|
| `/ws` | conexão | Handshake WebSocket |
| `/app/chat.send` | cliente → servidor | Enviar mensagem privada |
| `/user/queue/messages` | servidor → cliente | Receber mensagens e confirmações privadas |

Os nomes são candidatos e ainda podem mudar durante a implementação.

### Regras mínimas de segurança

- somente os dois participantes podem acessar a conversa e seu histórico;
- não confiar no remetente informado pelo payload; ele vem da identidade autenticada;
- usuários bloqueados não podem iniciar conversa nem enviar novas mensagens;
- mensagens passam pelo mesmo pipeline de sanitização e linguagem da comunidade;
- aplicar rate limit por usuário, conversa e conexão;
- denúncias preservam a mensagem original para revisão, mesmo após exclusão lógica;
- destinos privados não podem ser escolhidos livremente para acessar outro usuário;
- validar origem no handshake e autorização em cada mensagem recebida;
- limitar tamanho do frame e da mensagem;
- presença online e confirmação de leitura não devem expor usuários bloqueados.

Para a primeira versão, o broker simples em memória do Spring pode atender uma única instância. Se o backend for escalado horizontalmente, será necessário avaliar um broker externo e coordenação de sessões para que mensagens alcancem usuários conectados em instâncias diferentes.

## Métricas futuras

- publicações e comentários por período;
- usuários ativos na comunidade;
- perguntas resolvidas;
- tempo médio até a primeira resposta;
- taxa e motivos de denúncias;
- publicações vinculadas a cursos e projetos;
- retenção após a primeira interação.

## Roadmap sugerido

### Fase 1 — Fundação

- publicações;
- tags;
- comentários;
- paginação e filtros básicos;
- autorização por autor.

### Fase 2 — Engajamento

- reações;
- respostas;
- solução de perguntas;
- vínculo com projetos e cursos.

### Fase 3 — Segurança operacional

- denúncias;
- fila de moderação;
- histórico de decisões;
- bloqueio de comentários.

### Fase 4 — Descoberta

- busca textual;
- relevância;
- feed personalizado;
- notificações.

## Decisões pendentes

- O feed e a leitura de publicações serão públicos ou exigirão autenticação?
- Publicações aceitarão somente Markdown ou também anexos e imagens?
- Tags serão livres, moderadas ou criadas apenas por administradores?
- Fóruns temáticos entrarão no MVP ou em uma entrega posterior?
- Um post poderá pertencer a apenas um fórum ou ser publicado em vários?
- Projetos privados poderão ser vinculados a publicações da comunidade?
- Professores poderão moderar discussões vinculadas aos próprios cursos?
- Haverá uma role global `MODERATOR`?
- Quais serão os limites de comentários, reações e denúncias por intervalo?
- Notificações entrarão neste domínio ou em um módulo independente?
- Qual será a fórmula e o peso de cada infração no trust score?
- Quem poderá visualizar o percentual exato do trust score?
- A suspensão em `50%` bloqueará toda a plataforma ou somente recursos da comunidade?
- Qual será o prazo e o fluxo para recurso de um banimento permanente?
- Quais identificadores poderão compor a lista interna de bloqueio e por quanto tempo serão retidos?
- Quais categorias de palavras serão bloqueadas e quais apenas gerarão aviso ou revisão?
- O filtro será aplicado a posts, comentários, nomes de perfil e conteúdo de projetos?
- Qual penalidade uma ocorrência confirmada terá no trust score?
- Quais regras de proteção contra links entrarão no MVP?
- Qual será o limite de mensagens por minuto e por conversa?
- Mensagens poderão ser editadas e por quanto tempo?
- Usuários poderão apagar uma mensagem apenas para si ou para os dois participantes?
- Haverá confirmação de entrega, leitura e indicador de digitação no MVP?
- Por quanto tempo mensagens de chat serão mantidas?

## Registro de decisões

Use esta seção para evitar que decisões importantes se percam durante a implementação.

| Data | Decisão | Motivo | Status |
|---|---|---|---|
| — | Documento inicial criado | Centralizar o planejamento da comunidade | Proposto |
| 2026-07-20 | O núcleo terá feed, posts, comentários, curtidas e reações | Definição inicial do produto | Confirmado |
| 2026-07-20 | A moderação não utilizará inteligência artificial | Manter decisões previsíveis, humanas e auditáveis | Confirmado |
| 2026-07-20 | Posts e comentários poderão ser denunciados | Permitir que a comunidade encaminhe conteúdo para análise humana | Confirmado |
| 2026-07-20 | Trust score de `50%` ou menos suspende a conta imediatamente para revisão | Interromper preventivamente contas com perda relevante de confiança | Confirmado |
| 2026-07-20 | Score confirmado de `35%` ou menos após revisão resulta em perda permanente de acesso e lista interna de bloqueio | Aplicar sanção definitiva somente após decisão humana | Confirmado |
| 2026-07-20 | O trust score recupera `10` pontos percentuais por semana sem infração confirmada, limitado a `100%` | Permitir recuperação gradual de confiança | Confirmado |
| 2026-07-20 | O tratamento de palavras inadequadas será definido antes da implementação | Evitar falsos positivos e penalidades sem contexto | Em discussão |
| 2026-07-20 | Posts e comentários passarão por sanitização e filtro de linguagem antes da publicação | Proteger a aplicação e aplicar as regras da comunidade no backend | Confirmado |
| 2026-07-20 | O filtro usará uma cópia normalizada e mapa de substituições como `0 → o` | Detectar tentativas de contornar o filtro sem alterar o texto original | Confirmado |
| 2026-07-20 | Cada usuário poderá criar no máximo `5` posts em uma janela móvel de `24 horas` | Evitar spam e crescimento artificial do feed | Confirmado |
| 2026-07-20 | Bloqueio e silenciamento entre usuários farão parte da comunidade | Dar controle individual sobre interações indesejadas | Confirmado |
| 2026-07-20 | Sanções serão progressivas e terão mecanismo de recurso | Manter proporcionalidade e permitir correção de decisões | Confirmado |
| 2026-07-20 | Posts e comentários usarão exclusão lógica | Preservar contexto, auditoria e evidências de moderação | Confirmado |
| 2026-07-20 | Curtidas e reações terão proteção contra duplicação e manipulação | Preservar a integridade dos contadores e da reputação | Confirmado |
| 2026-07-20 | O trust score permanecerá representado pelo percentual exato | A representação por estados abstratos foi descartada | Confirmado |
| 2026-07-20 | Proteção adicional contra links | Definir limites e tratamento sem bloquear compartilhamentos legítimos | Em discussão |
| 2026-07-20 | O sistema terá chat privado entre dois usuários via WebSocket | Integrar comunicação direta à experiência da comunidade | Confirmado |
| 2026-07-20 | O chat será um domínio separado conectado à comunidade | Isolar persistência, tempo real e regras específicas de mensagens | Confirmado |
| 2026-07-20 | STOMP sobre WebSocket é a abordagem técnica inicial | Aproveitar roteamento, subscriptions e destinos privados do Spring | Proposto |
| 2026-07-20 | O chat será textual e em tempo real, sem chamadas de áudio ou vídeo | Manter o escopo focado em mensagens e evitar infraestrutura de mídia | Confirmado |

## Ideias e backlog livre

- seguir tags ou autores;
- salvar publicações para leitura posterior;
- medalhas temáticas e de conquista, como `Java Winner`, vinculadas a critérios verificáveis;
- página de medalhas do usuário e histórico de concessão;
- tendências (`trends`) de fóruns, tags, posts ou assuntos, com definição ainda pendente;
- desafios técnicos individuais ou comunitários;
- participação, submissão, prazo, regras e vencedores dos desafios;
- menções a usuários;
- notificações de respostas e solução marcada;
- coleções de recursos;
- eventos e desafios da comunidade;
- destaque semanal de projetos;
- reputação baseada em contribuições verificáveis;
- integração com progresso de aprendizado sem expor dados privados.

### Notas sobre itens do backlog

#### Medalhas

As medalhas podem ser concedidas automaticamente por critérios objetivos ou manualmente em desafios. Exemplos: `Java Winner`, `First Helpful Answer`, `Challenge Winner` e `Community Contributor`. A concessão precisa registrar critério, data e origem para impedir atribuições arbitrárias ou duplicadas.

#### Trends

Tendências podem ser calculadas sem IA usando uma pontuação determinística baseada em atividade recente, por exemplo:

- visualizações únicas;
- comentários;
- curtidas e reações;
- velocidade de crescimento;
- quantidade de participantes distintos;
- redução gradual do peso conforme o conteúdo envelhece.

A fórmula deve reduzir o impacto de ações repetidas pelo mesmo usuário e excluir conteúdo oculto, removido ou identificado como manipulado.

#### Desafios

Desafios podem ter título, descrição, regras, tecnologia ou fórum relacionado, período de inscrição, prazo de submissão e critérios de avaliação. Submissões podem apontar para projetos do próprio DevNest, permitindo reaproveitar o domínio `project`. A escolha de vencedores e a concessão de medalhas precisam ser auditáveis.
