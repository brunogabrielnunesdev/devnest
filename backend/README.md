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

Os documentos da comunidade representam planejamento. Funcionalidades planejadas só passam para a documentação da API depois de implementadas e validadas.

## Executando localmente

Pré-requisitos:

- JDK 21;
- PostgreSQL;
- variável `JWT_SECRET` com um segredo adequado para assinatura dos tokens;
- variável `FRONTEND_URL` com a origem permitida pelo CORS.

Por padrão, a aplicação usa o banco `devnest` em `localhost:5500`, com usuário e senha `devnest`. Esses valores estão em `src/main/resources/application.yaml`.

No Windows:

```powershell
$env:JWT_SECRET = "substitua-por-um-segredo-seguro"
$env:FRONTEND_URL = "http://localhost:5173"
.\mvnw.cmd spring-boot:run
```

Em Linux ou macOS:

```bash
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
