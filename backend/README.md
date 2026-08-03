# DevNest Backend

API REST da plataforma DevNest para aprendizado, publicação de cursos e colaboração em projetos de desenvolvimento.

## Funcionalidades

- autenticação stateless com access token e refresh token JWT;
- autorização por papéis (`ADMIN`, `TEACHER` e `STUDENT`) e por ownership;
- criação de cursos, módulos, aulas, quizzes, perguntas e alternativas;
- matrícula, progresso, conteúdo de aprendizado e tentativas de quiz;
- comentários em aulas e moderação por professores e administradores;
- perfil do usuário e troca de senha;
- projetos colaborativos com membros, tarefas, notas, atualizações e log de atividades;
- comunidade com fóruns, posts, comentários, reações, bloqueios e silenciamentos;
- filtro de conteúdo, rate limits, detecção de duplicidade e proteção concorrente;
- denúncias de conteúdo com fila e decisão administrativa auditável;
- dashboards e métricas para administradores, professores e alunos.

## Stack

- Java 21;
- Spring Boot 4;
- Spring MVC, Spring Security e Spring Data JPA;
- PostgreSQL e Flyway;
- JWT (Auth0 Java JWT);
- MapStruct e Lombok;
- Maven;
- H2 nos testes.

## Arquitetura

O código é organizado primeiro por domínio e, dentro de cada domínio, por camada:

```text
com.devnest
├── admin       # administração de usuários, cursos, comentários e métricas
├── auth        # autenticação, JWT e configuração do Spring Security
├── common      # configuração, entidade-base e tratamento global de erros
├── community   # fóruns, conteúdo, interações e proteções anti-spam
├── course      # cursos, conteúdo, quizzes, matrículas, progresso e comentários
├── identity    # usuário, papéis, status e persistência da identidade
├── profile     # perfil do usuário autenticado
└── project     # projetos, membros, tarefas, notas, atualizações e atividades
```

Controllers definem o contrato HTTP, services concentram regras de negócio e autorização por recurso, repositories cuidam da persistência e DTOs evitam expor entidades JPA na API.

## Documentação

- [Arquitetura](docs/ARCHITECTURE.md): domínios, camadas, segurança e persistência atuais.
- [API](docs/API.md): endpoints atualmente implementados no backend.
- [Comunidade](docs/COMMUNITY.md): requisitos, decisões de produto e backlog da comunidade.
- [Plano de execução da comunidade](docs/COMMUNITY_EXECUTION_PLAN.md): fases, regras de negócio, migrations, testes e critérios de aceite.

Os documentos da comunidade distinguem o que já foi implementado do backlog. Funcionalidades planejadas só passam para a documentação da API depois de implementadas e validadas.

## Executando localmente

Pré-requisitos:

- JDK 21;
- PostgreSQL;
- variáveis `URL_DB`, `USER_DB` e `PASSWORD_DB` para conexão com o PostgreSQL;
- variável `JWT_SECRET` com um segredo adequado para assinatura dos tokens;
- variável `FRONTEND_URL` com a origem permitida pelo CORS.

Limites operacionais da comunidade podem ser sobrescritos por:

- `COMMUNITY_POSTS_PER_24_HOURS`;
- `COMMUNITY_COMMENTS_PER_MINUTE`;
- `COMMUNITY_REACTIONS_PER_MINUTE`;
- `COMMUNITY_DUPLICATE_WINDOW_MINUTES`.

No Windows:

```powershell
$env:URL_DB = "jdbc:postgresql://localhost:5432/devnest"
$env:USER_DB = "devnest"
$env:PASSWORD_DB = "substitua-por-uma-senha-segura"
$env:JWT_SECRET = "substitua-por-um-segredo-seguro"
$env:FRONTEND_URL = "http://localhost:5173"
.\mvnw.cmd spring-boot:run
```

Em Linux ou macOS:

```bash
export URL_DB="jdbc:postgresql://localhost:5432/devnest"
export USER_DB="devnest"
export PASSWORD_DB="substitua-por-uma-senha-segura"
export JWT_SECRET="substitua-por-um-segredo-seguro"
export FRONTEND_URL="http://localhost:5173"
./mvnw spring-boot:run
```

As migrations do Flyway são executadas na inicialização. A API não possui um prefixo global configurado; exemplos de rotas são `/auth/login`, `/course/catalog` e `/projects`.

## Testes

Os testes usam H2 em memória, em modo de compatibilidade com PostgreSQL, e não exigem um banco local.

```powershell
.\mvnw.cmd test
```

Os cenários concorrentes de posts e reações também foram validados em PostgreSQL 16 descartável, com as migrations `V1` a `V11` aplicadas em ordem e o schema validado pelo Hibernate.

## Docker

O `Dockerfile` produz uma imagem em dois estágios: compila o JAR com Maven e o executa sobre o JRE 21.

```bash
docker build -t devnest-backend .
docker run --rm -p 8080:8080 \
  -e JWT_SECRET="substitua-por-um-segredo-seguro" \
  -e FRONTEND_URL="http://localhost:5173" \
  devnest-backend
```

O repositório do backend não contém atualmente um `docker-compose.yml`; portanto, o PostgreSQL deve estar acessível separadamente e conforme a URL configurada.

## Autor

Bruno Gabriel Nunes
