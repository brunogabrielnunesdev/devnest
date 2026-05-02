# DevNest

DevNest e uma plataforma de aprendizagem e pratica para desenvolvedores, com foco em cursos, progresso de estudo, quizzes, comentarios e projetos.

Este repositorio esta organizado como um monorepo simples, separado entre backend e frontend.

## Objetivo do projeto

O objetivo do DevNest e reunir, em uma unica aplicacao:

- trilhas de aprendizado em formato de cursos
- progresso de aulas por aluno
- quizzes para validacao de aprendizado
- comentarios e moderacao
- projetos para pratica e acompanhamento de evolucao

## Estrutura do repositorio

```text
devnest/
  backend/                 API em Spring Boot
  frontend/
    devnestfrontend/       Aplicacao web em React + Vite
```

## Stack principal

### Backend

- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA
- Flyway
- PostgreSQL
- H2 para testes
- JUnit

### Frontend

- React
- Vite
- React Router
- Tailwind CSS
- Framer Motion
- Axios

## Funcionalidades principais

No estado atual do projeto, os modulos principais incluem:

- autenticacao de usuarios
- perfil de usuario
- criacao e publicacao de cursos
- modulos e aulas
- quizzes por aula
- matricula e progresso do aluno
- comentarios em aulas com moderacao
- projetos e atualizacoes de projeto

## Regras de negocio importantes

Algumas regras relevantes ja implementadas:

- apenas usuarios autenticados acessam rotas protegidas
- refresh token possui controle de invalidacao por versao de token
- o cadastro nao expoe explicitamente quando um email ja existe
- a revisao detalhada do quiz so aparece quando o aluno passa ou esgota as tentativas
- aulas com quiz exigem quiz aprovado para serem concluidas

## Como executar o projeto

### Backend

Entre na pasta:

```powershell
cd backend
```

Execute a aplicacao:

```powershell
.\mvnw.cmd spring-boot:run
```

Execute os testes:

```powershell
.\mvnw.cmd test
```

### Frontend

Entre na pasta:

```powershell
cd frontend\devnestfrontend
```

Instale as dependencias:

```powershell
npm install
```

Execute em desenvolvimento:

```powershell
npm run dev
```

Gere o build:

```powershell
npm run build
```

## Estrutura geral da aplicacao

### Backend

O backend esta organizado em modulos por dominio, como:

- `auth`
- `identity`
- `profile`
- `course`
- `project`
- `admin`
- `common`

### Frontend

O frontend esta organizado em:

- `pages`
- `components`
- `services`
- `routes`
- `lib`

## Status atual

O projeto esta em fase de consolidacao e padronizacao.

Hoje ele ja possui base funcional para ser tratado como MVP tecnico, mas ainda pode evoluir em:

- documentacao mais detalhada
- integracao frontend/backend
- refinamento de UX
- organizacao final para apresentacao

## Proximos passos sugeridos

- revisar e padronizar nomenclaturas
- alinhar contratos entre backend e frontend
- documentar melhor fluxos principais
- validar os cenarios centrais com perfil de professor e aluno

## Observacoes

- O frontend principal esta em `frontend/devnestfrontend`.
- O backend possui testes automatizados para os fluxos principais.
- Este README e uma base inicial e pode ser expandido com arquitetura, API, regras de negocio e instrucoes de deploy.
