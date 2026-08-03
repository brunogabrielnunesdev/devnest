# Arquitetura do backend

Este documento descreve a arquitetura implementada atualmente no backend do DevNest.

## Visão geral

A aplicação é um monólito modular Spring Boot. A organização principal é por domínio (package by feature), com separação interna entre controllers, DTOs, entities, mappers, repositories e services quando essas camadas são necessárias.

```text
Cliente HTTP
    │
    ▼
SecurityFilterChain → JwtAuthenticationFilter
    │
    ▼
Controller → Service → Repository → PostgreSQL
                 │
                 ├── Access service (papel e acesso ao recurso)
                 └── Mapper → DTO de resposta
```

## Domínios

### `com.devnest.auth`

Autenticação e infraestrutura de segurança:

- endpoints de registro, login e renovação de tokens;
- emissão e validação de access e refresh tokens JWT;
- filtro de autenticação, implementação da identidade autenticada e configuração do Spring Security.

### `com.devnest.identity`

Núcleo compartilhado da identidade:

- entidades `User` e `UserProfile`;
- enums `UserRole` e `UserStatus`;
- `UserRepository`.

Ele é usado pelos módulos de autenticação, perfil, cursos, projetos e administração.

### `com.devnest.profile`

Consulta e alteração do perfil do usuário autenticado, incluindo troca de senha.

### `com.devnest.course`

Domínio educacional:

- autoria de cursos, módulos, aulas, quizzes, perguntas e alternativas;
- catálogo de cursos publicados;
- matrículas, conteúdo de aprendizado e progresso de aulas;
- submissão e histórico de tentativas de quizzes;
- comentários e moderação;
- métricas de professores e alunos.

Os controllers são separados por jornada (`student` e `teacher`); a área de autoria do professor fica em `teacher.teacherworkspace`.

### `com.devnest.project`

Domínio de colaboração em projetos:

- projetos e atualizações de progresso;
- membros com papéis `OWNER`, `ADMIN`, `MEMBER` e `VIEWER`;
- tarefas com status, prioridade, responsável e prazo;
- notas vinculadas ao autor;
- registro de atividades do projeto.

O `ProjectAccessService` centraliza a busca do usuário atual e as permissões por projeto. Alterações relevantes geram registros por meio do `ActivityLogService`.

### `com.devnest.community`

Dominio da comunidade:

- foruns administraveis e feed paginado;
- posts associados a foruns, tags, projetos ou cursos;
- comentarios paginados com ownership e exclusao logica;
- reacoes em posts e comentarios com contadores agregados e unicidade no banco;
- bloqueios bidirecionais para interacoes e silenciamento unilateral aplicado ao feed;
- retencao de posts e comentarios pelo filtro de conteudo configuravel;
- limites temporais configuraveis usando um `Clock` injetavel;
- eventos persistidos de rate limit para comentarios e reacoes;
- deteccao deterministica de conteudo duplicado por autor;
- bloqueio pessimista por ator para serializar operacoes concorrentes.
- denuncias unicas por usuario e alvo, com fila administrativa paginada e decisao auditavel;
- casos de moderacao abertos por denuncias confirmadas e acoes imutaveis com estado anterior e novo;
- bloqueios pessimistas nos casos e conteudos para serializar decisoes administrativas concorrentes.

O `AccessService` centraliza identidade, papel administrativo e ownership. O `CommunityActorLockService` bloqueia a linha do usuario durante operacoes sensiveis a concorrencia. `CommunityRateLimitService` registra eventos em `community_rate_limit_events`, enquanto `DuplicateContentService` compara uma representacao normalizada do conteudo recente. Entidades com nomes compartilhados com outros dominios usam nomes explicitos de entidade e bean para evitar colisoes no contexto Spring e no JPA.

### `com.devnest.admin`

Operações restritas a administradores:

- gestão de usuários e papéis;
- arquivamento, restauração e remoção de cursos;
- moderação de comentários;
- métricas administrativas.

O `AccessService` contém verificações auxiliares usadas pelo domínio administrativo.

### `com.devnest.common`

Infraestrutura transversal:

- `BaseEntity`, com os campos comuns das entidades;
- configuração de CORS;
- exceções de aplicação;
- `GlobalExceptionHandler` e contrato `ApiError`.

## Responsabilidades das camadas

- **Controller:** mapeia HTTP, valida DTOs de entrada, declara autorização de método e delega a execução.
- **Service:** aplica regras de negócio, transações e autorização dependente do recurso.
- **Access service:** resolve o usuário autenticado e valida ownership, matrícula ou participação.
- **Repository:** implementa persistência e consultas com Spring Data JPA.
- **Entity:** representa o modelo persistido e seus relacionamentos.
- **DTO:** define contratos de entrada e saída sem expor entidades; a maioria é implementada como `record`.
- **Mapper:** converte entidades em DTOs. O módulo de cursos usa MapStruct; o módulo de projetos também possui mapeadores próprios.

## Segurança

O backend é stateless, desabilita CSRF, habilita CORS e executa `JwtAuthenticationFilter` antes de `UsernamePasswordAuthenticationFilter`.

No nível HTTP:

- `POST /auth/register`, `/auth/login` e `/auth/refresh` são públicos;
- `GET /course/catalog` e `/course/catalog/**` são públicos;
- `/admin/**` exige `ADMIN`;
- qualquer outra rota exige autenticação.

No nível de método, `@PreAuthorize` restringe jornadas de professor, aluno e administrador. Nos services, access services complementam essa proteção com regras como ownership do curso, matrícula ou participação e papel no projeto.

O JWT transporta a identificação e os dados necessários da identidade autenticada. A versão do token armazenada no usuário permite invalidar tokens antigos durante a rotação de refresh tokens.

## Persistência

Em execução normal:

- PostgreSQL é o banco relacional;
- Flyway aplica migrations versionadas em `src/main/resources/db/migration`;
- a configuração JPA desabilita Open Session in View.

As migrations cobrem o modelo inicial, versionamento de token, flags administrativas, capa de curso, recursos colaborativos de projetos, limites de quizzes e as tabelas da comunidade de `V7` a `V13`.

Nos testes regulares, o sistema usa H2 em memória com compatibilidade PostgreSQL, `ddl-auto: create-drop` e Flyway desabilitado. Os cenários concorrentes e as migrations `V1` a `V11` também foram validados em PostgreSQL 16 descartável.

## Tratamento de erros

Exceções de validação, autenticação, autorização, recurso inexistente e conflito são convertidas pelo `GlobalExceptionHandler` no contrato uniforme `ApiError`. Controllers e services não precisam duplicar a montagem das respostas de erro.

## Configuração e execução

As propriedades ficam em `application.yaml`. As configurações externas obrigatórias são:

- `JWT_SECRET`, usado para assinar e validar JWTs;
- `FRONTEND_URL`, incluído nas origens permitidas pelo CORS.

O `Dockerfile` usa build multi-stage com Maven 3.9/JDK 21 e uma imagem final JRE 21. Não há `docker-compose.yml` neste repositório do backend.
