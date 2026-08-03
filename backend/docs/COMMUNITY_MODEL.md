# Modelagem da comunidade — primeiro incremento

> Modelo inicial de fóruns, posts e feed. Esse incremento já foi implementado; evoluções posteriores estão registradas ao final do documento.

## Escopo

O primeiro incremento contém:

- fóruns temáticos desde a primeira entrega;
- posts pertencentes a exatamente um fórum;
- feed geral e feed por fórum;
- acesso exclusivamente autenticado;
- vínculo opcional do post com um projeto ou curso;
- tags reutilizáveis;
- exclusão lógica e bloqueio de novas interações;
- estrutura preparada para comentários, reações e moderação posteriores.

Não pertencem a este incremento comentários, reações, denúncias, trust score e chat. Esses recursos serão modelados detalhadamente antes de suas respectivas etapas.

## Evoluções implementadas após o primeiro incremento

- comentários, reações, bloqueios e silenciamentos;
- filtro configurável e retenção de conteúdo para revisão;
- `community_rate_limit_events`, criada pela migration `V11`;
- rate limits configuráveis para comentários e reações;
- detecção de posts e comentários duplicados em janela temporal;
- bloqueio pessimista da linha do usuário para preservar limites sob concorrência.

Denúncias, trust score, moderação avançada e chat ainda não fazem parte do modelo implementado.

## Relacionamentos

```text
User 1 ─────── N CommunityForum : createdBy
User 1 ─────── N CommunityPost  : author

CommunityForum 1 ─────── N CommunityPost
CommunityPost  N ─────── N CommunityTag

Project 0..1 ─────── N CommunityPost
Course  0..1 ─────── N CommunityPost
```

Regras estruturais:

- todo post possui um autor e um fórum;
- um post não pode pertencer a vários fóruns;
- projeto e curso são vínculos opcionais e independentes;
- apagar ou arquivar um fórum não apaga seus posts;
- apagar um usuário não remove fisicamente seus fóruns ou posts;
- tags não pertencem a um fórum específico;
- entidades relacionadas são carregadas de forma lazy.

## `CommunityForum`

Tabela: `community_forums`.

| Campo | Tipo | Regra |
|---|---|---|
| `id` | `UUID` | chave primária |
| `createdBy` | `User` | obrigatório, FK `created_by_id` |
| `name` | `String(80)` | obrigatório |
| `slug` | `String(100)` | obrigatório e único |
| `description` | `String(500)` | obrigatório |
| `status` | `CommunityForumStatus` | obrigatório |
| `createdAt` | `OffsetDateTime` | herdado de `BaseEntity` |
| `updatedAt` | `OffsetDateTime` | herdado de `BaseEntity` |

`CommunityForumStatus`:

```text
ACTIVE
ARCHIVED
```

Transições:

```text
ACTIVE → ARCHIVED
ARCHIVED → ACTIVE
```

Um fórum arquivado permanece consultável, mas não aceita novos posts. Criação, edição, arquivamento e restauração são operações administrativas.

## `CommunityPost`

Tabela: `community_posts`.

| Campo | Tipo | Regra |
|---|---|---|
| `id` | `UUID` | chave primária |
| `forum` | `CommunityForum` | obrigatório, FK `forum_id` |
| `author` | `User` | obrigatório, FK `author_id` |
| `project` | `Project?` | opcional, FK `project_id` |
| `course` | `Course?` | opcional, FK `course_id` |
| `title` | `String(160)` | obrigatório |
| `content` | `text` | obrigatório, máximo lógico de 20.000 caracteres |
| `type` | `CommunityPostType` | obrigatório |
| `status` | `CommunityContentStatus` | obrigatório |
| `commentsLocked` | `boolean` | obrigatório, inicialmente `false` |
| `removedBy` | `User?` | ator da remoção, quando aplicável |
| `removedAt` | `OffsetDateTime?` | data da remoção lógica |
| `removalReason` | `String(500)?` | motivo administrativo ou do sistema |
| `createdAt` | `OffsetDateTime` | herdado de `BaseEntity` |
| `updatedAt` | `OffsetDateTime` | herdado de `BaseEntity` |

`CommunityPostType`:

```text
DISCUSSION
QUESTION
PROJECT_SHOWCASE
RESOURCE
```

`CommunityContentStatus`:

```text
ACTIVE
HELD_FOR_REVIEW
HIDDEN
REMOVED
```

Semântica dos estados:

- `ACTIVE`: visível e disponível para interação;
- `HELD_FOR_REVIEW`: ainda não publicado e aguardando decisão;
- `HIDDEN`: temporariamente invisível nas consultas comuns;
- `REMOVED`: exclusão lógica definitiva, preservada para auditoria.

Somente posts `ACTIVE` aparecem no feed comum. `commentsLocked` impede novas interações sem alterar a visibilidade do post.

## `CommunityTag`

Tabela: `community_tags`.

| Campo | Tipo | Regra |
|---|---|---|
| `id` | `UUID` | chave primária |
| `name` | `String(50)` | obrigatório |
| `slug` | `String(60)` | obrigatório e único |
| `createdAt` | `OffsetDateTime` | herdado de `BaseEntity` |
| `updatedAt` | `OffsetDateTime` | herdado de `BaseEntity` |

A associação usa a tabela `community_post_tags`, com chave primária composta por `post_id` e `tag_id`.

Tags podem ser propostas pelos usuários durante a criação ou edição de um post. Antes da persistência, o nome da tag passa por:

- remoção de espaços excedentes;
- normalização Unicode e conversão para minúsculas para gerar o slug;
- filtro de palavras inadequadas, incluindo a normalização contra tentativas de contorno;
- consulta pelo slug para reutilizar uma tag existente em vez de duplicá-la.

Uma tag rejeitada impede a associação e retorna erro de validação. A lista e as regras do filtro não ficam hardcoded na entidade ou no service de posts.

## Constraints e índices

Constraints necessárias:

- unicidade de `community_forums.slug`;
- unicidade de `community_tags.slug`;
- unicidade do par `(post_id, tag_id)` em `community_post_tags`;
- FKs obrigatórias para fórum e autor do post;
- FKs opcionais para projeto, curso e responsável pela remoção;
- enums persistidos como texto;
- `comments_locked` com valor padrão `false`.

Índices iniciais:

- `community_posts(status, created_at desc, id desc)` para o feed geral;
- `community_posts(forum_id, status, created_at desc, id desc)` para o feed por fórum;
- `community_posts(author_id, status, created_at desc)` para posts do usuário e limite de criação;
- `community_posts(project_id)` e `community_posts(course_id)` para vínculos opcionais;
- `community_post_tags(tag_id, post_id)` para filtro por tag;
- `community_forums(status, name)` para listagem dos fóruns.

O primeiro incremento usa paginação tradicional baseada em `page` e `size`, com `created_at` e `id` como ordenação determinística. Paginação por cursor poderá ser adotada posteriormente se o volume tornar a paginação profunda inadequada.

## Invariantes do domínio

- somente usuário `ACTIVE` cria ou altera conteúdo;
- todas as operações da comunidade exigem autenticação;
- somente administradores administram fóruns;
- um usuário cria no máximo cinco posts em uma janela móvel de 24 horas;
- post novo só pode ser criado em fórum `ACTIVE`;
- autor pode editar ou remover logicamente o próprio post enquanto estiver autorizado na plataforma;
- conteúdo removido não volta ao feed e não aceita interações;
- somente projetos com visibilidade `PUBLIC` podem ser vinculados a posts;
- cursos podem ser vinculados independentemente do status, inclusive quando estiverem em rascunho;
- vincular um curso em rascunho não autoriza expor aulas, módulos ou outros dados internos do curso;
- tags propostas por usuários passam por normalização e filtro de palavras inadequadas;
- sanitização e filtro de linguagem devem ocorrer antes da persistência publicável.

## Impacto global do trust score

A suspensão por trust score afeta toda a plataforma. Antes da fase de moderação, a modelagem de identidade deverá evoluir para distinguir pelo menos:

```text
UserStatus: ACTIVE, SUSPENDED, BLOCKED, DELETED
```

- `SUSPENDED`: bloqueio reversível que exige revisão manual;
- `BLOCKED`: perda permanente de acesso após revisão;
- alteração para `SUSPENDED` ou `BLOCKED` incrementa `tokenVersion`, invalidando tokens existentes;
- recuperação automática do score não altera o status da conta.

Essa alteração está registrada aqui como impacto futuro e não faz parte da implementação do primeiro incremento.

## Decisões fechadas para o incremento

- projetos privados não podem ser vinculados a posts;
- cursos podem ser vinculados mesmo quando estiverem em rascunho;
- usuários podem propor tags, que passam por normalização e filtro de palavras inadequadas;
- o feed começa com paginação tradicional por `page` e `size`.
