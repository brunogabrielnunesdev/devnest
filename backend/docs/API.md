# API Documentation

Documentacao tecnica resumida dos principais endpoints do backend do DevNest.

## Regras Gerais de Autenticacao

- Endpoints de autenticacao sao publicos.
- Os demais endpoints exigem bearer token JWT.
- O token deve ser enviado no header `Authorization`.
- O backend usa `@PreAuthorize` para explicitar roles e os services validam ownership quando necessario.

Exemplo:

```http
Authorization: Bearer <access-token>
```

## Roles

- `ADMIN`
- `TEACHER`
- `STUDENT`

## Fluxo de Autenticacao

1. `POST /auth/register` cria uma conta de aluno e retorna tokens.
2. `POST /auth/login` autentica um usuario existente e retorna tokens.
3. `POST /auth/refresh` recebe um refresh token valido e retorna um novo par de tokens.
4. O access token e usado nas rotas protegidas.

## Principais Endpoints

### Auth

| Metodo | Endpoint         | Auth    | Role |
|-------|------------------|---------|-----|
| POST  | `/auth/register` | Publico | -   |
| POST  | `/auth/login`    | Publico | -   |
| POST  | `/auth/refresh`  | Publico | -   |

Request de registro:

```json
{
  "email": "student@example.com",
  "password": "secret123",
  "displayName": "Student"
}
```

Response:

```json
{
  "accessToken": "jwt-access-token",
  "refreshToken": "jwt-refresh-token"
}
```

### Profile

| Metodo | Endpoint | Auth | Role |
|---|---|---|---|
| GET | `/perfil` | Sim | autenticado |
| PATCH | `/perfil` | Sim | autenticado |
| POST | `/perfil/senha` | Sim | autenticado |

### Courses

#### Catalogo e consulta publicada

| Metodo | Endpoint | Auth | Role |
|---|---|---|---|
| GET | `/course/catalog` | Nao | publico |
| GET | `/course/catalog/{courseId}` | Nao | publico |

#### Gestao do professor

| Metodo | Endpoint | Auth | Role |
|---|---|---|---|
| POST | `/course` | Sim | `TEACHER` |
| GET | `/course/my` | Sim | `TEACHER` |
| GET | `/course/{courseId}` | Sim | `TEACHER` + owner |
| PUT | `/course/{courseId}` | Sim | `TEACHER` + owner |
| DELETE | `/course/{courseId}` | Sim | `TEACHER` + owner |
| POST | `/course/{courseId}/publish` | Sim | `TEACHER` + owner |

Request de criacao de curso:

```json
{
  "title": "Java Fundamentals",
  "description": "Introductory Java course",
  "level": "BEGINNER",
  "coverImageUrl": "https://cdn.example.com/courses/java.jpg"
}
```

Response:

```json
{
  "id": "uuid",
  "teacherId": "uuid",
  "title": "Java Fundamentals",
  "description": "Introductory Java course",
  "level": "BEGINNER",
  "coverImageUrl": "https://cdn.example.com/courses/java.jpg",
  "status": "DRAFT",
  "createdAt": "2026-05-12T20:00:00Z",
  "updatedAt": "2026-05-12T20:00:00Z"
}
```

### Modules

| Metodo | Endpoint | Auth | Role |
|---|---|---|---|
| POST | `/course/{courseId}/module` | Sim | `TEACHER` + owner |
| GET | `/course/{courseId}/module` | Sim | `TEACHER` + owner |
| GET | `/course/{courseId}/module/{moduleId}` | Sim | `TEACHER` + owner |
| PUT | `/course/{courseId}/module/{moduleId}` | Sim | `TEACHER` + owner |
| DELETE | `/course/{courseId}/module/{moduleId}` | Sim | `TEACHER` + owner |

### Lessons

| Metodo | Endpoint | Auth | Role |
|---|---|---|---|
| POST | `/course/{courseId}/module/{moduleId}/lesson` | Sim | `TEACHER` + owner |
| GET | `/course/{courseId}/module/{moduleId}/lesson` | Sim | `TEACHER` + owner |
| GET | `/course/{courseId}/module/{moduleId}/lesson/{lessonId}` | Sim | `TEACHER` + owner |
| PUT | `/course/{courseId}/module/{moduleId}/lesson/{lessonId}` | Sim | `TEACHER` + owner |
| DELETE | `/course/{courseId}/module/{moduleId}/lesson/{lessonId}` | Sim | `TEACHER` + owner |

### Teacher Quiz Authoring

| Metodo | Endpoint | Auth | Role |
|---|---|---|---|
| POST | `/course/{courseId}/module/{moduleId}/lesson/{lessonId}/quiz` | Sim | `TEACHER` + owner |
| GET | `/course/{courseId}/module/{moduleId}/lesson/{lessonId}/quiz` | Sim | `TEACHER` + owner |
| PUT | `/course/{courseId}/module/{moduleId}/lesson/{lessonId}/quiz` | Sim | `TEACHER` + owner |
| DELETE | `/course/{courseId}/module/{moduleId}/lesson/{lessonId}/quiz` | Sim | `TEACHER` + owner |

#### Questions

| Metodo | Endpoint | Auth | Role |
|---|---|---|---|
| POST | `/course/{courseId}/module/{moduleId}/lesson/{lessonId}/quiz/question` | Sim | `TEACHER` + owner |
| GET | `/course/{courseId}/module/{moduleId}/lesson/{lessonId}/quiz/question` | Sim | `TEACHER` + owner |
| GET | `/course/{courseId}/module/{moduleId}/lesson/{lessonId}/quiz/question/{questionId}` | Sim | `TEACHER` + owner |
| PUT | `/course/{courseId}/module/{moduleId}/lesson/{lessonId}/quiz/question/{questionId}` | Sim | `TEACHER` + owner |
| DELETE | `/course/{courseId}/module/{moduleId}/lesson/{lessonId}/quiz/question/{questionId}` | Sim | `TEACHER` + owner |

#### Options

| Metodo | Endpoint | Auth | Role |
|---|---|---|---|
| POST | `/course/{courseId}/module/{moduleId}/lesson/{lessonId}/quiz/question/{questionId}/option` | Sim | `TEACHER` + owner |
| GET | `/course/{courseId}/module/{moduleId}/lesson/{lessonId}/quiz/question/{questionId}/option` | Sim | `TEACHER` + owner |
| GET | `/course/{courseId}/module/{moduleId}/lesson/{lessonId}/quiz/question/{questionId}/option/{optionId}` | Sim | `TEACHER` + owner |
| PUT | `/course/{courseId}/module/{moduleId}/lesson/{lessonId}/quiz/question/{questionId}/option/{optionId}` | Sim | `TEACHER` + owner |
| DELETE | `/course/{courseId}/module/{moduleId}/lesson/{lessonId}/quiz/question/{questionId}/option/{optionId}` | Sim | `TEACHER` + owner |

### Student Learning

| Metodo | Endpoint | Auth | Role |
|---|---|---|---|
| POST | `/course/{courseId}/enrollment` | Sim | `STUDENT` |
| GET | `/course/enrollment` | Sim | `STUDENT` |
| GET | `/course/{courseId}/enrollment` | Sim | `STUDENT` |
| POST | `/course/{courseId}/lesson/{lessonId}/progress` | Sim | `STUDENT` |
| GET | `/course/{courseId}/progress` | Sim | `STUDENT` |
| GET | `/course/{courseId}/learning-content` | Sim | `STUDENT` |

### Student Quiz

| Metodo | Endpoint | Auth | Role |
|---|---|---|---|
| GET | `/course/{courseId}/lesson/{lessonId}/quiz` | Sim | `STUDENT` |
| POST | `/course/{courseId}/lesson/{lessonId}/quiz/attempts` | Sim | `STUDENT` |
| GET | `/course/{courseId}/lesson/{lessonId}/quiz/attempts` | Sim | `STUDENT` |

Request de tentativa:

```json
{
  "answers": [
    {
      "questionId": "uuid",
      "selectedOptionId": "uuid"
    }
  ]
}
```

### Comments

#### Student comment flow

| Metodo | Endpoint | Auth | Role |
|---|---|---|---|
| POST | `/course/{courseId}/lesson/{lessonId}/comment` | Sim | `STUDENT` |
| GET | `/course/{courseId}/lesson/{lessonId}/comment` | Sim | autenticado |

#### Teacher moderation

| Metodo | Endpoint | Auth | Role |
|---|---|---|---|
| POST | `/course/{courseId}/module/{moduleId}/lesson/{lessonId}/comment/{commentId}/moderation` | Sim | `TEACHER` + owner |

### Projects

| Metodo | Endpoint | Auth | Role |
|---|---|---|---|
| POST | `/projects` | Sim | autenticado |
| GET | `/projects` | Sim | projetos acessiveis ao usuario |
| GET | `/projects/{projectId}` | Sim | participante ou projeto publico |
| PATCH | `/projects/{projectId}` | Sim | permissao no projeto |
| DELETE | `/projects/{projectId}` | Sim | permissao no projeto |

### Project Updates

| Metodo | Endpoint | Auth | Role |
|---|---|---|---|
| POST | `/projects/{projectId}/updates` | Sim | permissao no projeto |
| GET | `/projects/{projectId}/updates` | Sim | participante ou projeto publico |
| GET | `/projects/{projectId}/updates/{updateId}` | Sim | participante ou projeto publico |
| PATCH | `/projects/{projectId}/updates/{updateId}` | Sim | permissao no projeto |
| DELETE | `/projects/{projectId}/updates/{updateId}` | Sim | permissao no projeto |

### Project Members

| Metodo | Endpoint | Auth | Acesso |
|---|---|---|---|
| POST | `/projects/{projectId}/members` | Sim | administracao do projeto |
| GET | `/projects/{projectId}/members` | Sim | participante ou projeto publico |
| PATCH | `/projects/{projectId}/members/{memberId}` | Sim | administracao do projeto |
| DELETE | `/projects/{projectId}/members/{memberId}` | Sim | administracao do projeto |

Os papeis internos do projeto sao `OWNER`, `ADMIN`, `MEMBER` e `VIEWER`; eles sao independentes das roles globais da plataforma.

### Project Tasks

| Metodo | Endpoint | Auth | Acesso |
|---|---|---|---|
| POST | `/projects/{projectId}/tasks` | Sim | permissao de edicao |
| GET | `/projects/{projectId}/tasks` | Sim | participante ou projeto publico |
| PATCH | `/projects/{projectId}/tasks/{taskId}` | Sim | permissao de edicao |
| DELETE | `/projects/{projectId}/tasks/{taskId}` | Sim | permissao de edicao |

Tarefas usam os status `TODO`, `IN_PROGRESS` e `DONE` e as prioridades `LOW`, `MEDIUM` e `HIGH`.

### Project Notes

| Metodo | Endpoint | Auth | Acesso |
|---|---|---|---|
| POST | `/projects/{projectId}/notes` | Sim | permissao no projeto |
| GET | `/projects/{projectId}/notes` | Sim | participante ou projeto publico |
| PATCH | `/projects/{projectId}/notes/{noteId}` | Sim | permissao no projeto |
| DELETE | `/projects/{projectId}/notes/{noteId}` | Sim | permissao no projeto |

### Metrics

| Metodo | Endpoint | Auth | Role |
|---|---|---|---|
| GET | `/teacher/metrics` | Sim | `TEACHER` |
| GET | `/student/metrics` | Sim | `STUDENT` |
| GET | `/admin/metrics` | Sim | `ADMIN` |

### Comunidade

| Metodo | Endpoint | Auth | Acesso |
|---|---|---|---|
| GET | `/community/forums` | Sim | usuario autenticado |
| GET | `/community/forums/{slug}` | Sim | usuario autenticado |
| GET | `/community/forums/{slug}/posts` | Sim | usuario autenticado |
| GET | `/community/posts` | Sim | usuario autenticado |
| GET | `/community/posts/{postId}` | Sim | usuario autenticado |
| POST | `/community/forums/{forumId}/posts` | Sim | usuario autenticado |
| PATCH | `/community/posts/{postId}` | Sim | autor ou `ADMIN` |
| DELETE | `/community/posts/{postId}` | Sim | autor ou `ADMIN` |
| GET | `/community/posts/{postId}/comments` | Sim | usuario autenticado |
| POST | `/community/posts/{postId}/comments` | Sim | usuario autenticado |
| PATCH | `/community/comments/{commentId}` | Sim | autor ou `ADMIN` |
| DELETE | `/community/comments/{commentId}` | Sim | autor ou `ADMIN` |
| PUT | `/community/posts/{postId}/reaction` | Sim | usuario autenticado |
| DELETE | `/community/posts/{postId}/reaction` | Sim | usuario autenticado |
| GET | `/community/posts/{postId}/reactions` | Sim | usuario autenticado |
| PUT | `/community/comments/{commentId}/reaction` | Sim | usuario autenticado |
| DELETE | `/community/comments/{commentId}/reaction` | Sim | usuario autenticado |
| GET | `/community/comments/{commentId}/reactions` | Sim | usuario autenticado |
| PUT | `/community/users/{userId}/block` | Sim | usuario autenticado |
| DELETE | `/community/users/{userId}/block` | Sim | usuario autenticado |
| GET | `/community/users/blocked` | Sim | usuario autenticado |
| PUT | `/community/users/{userId}/mute` | Sim | usuario autenticado |
| DELETE | `/community/users/{userId}/mute` | Sim | usuario autenticado |
| GET | `/community/users/muted` | Sim | usuario autenticado |

Posts e comentarios sinalizados pelo filtro de conteudo ficam retidos para revisao e nao aparecem nas listagens publicas. A exclusao de ambos e logica.
Cada usuario mantem no maximo uma reacao por post ou comentario. Um novo `PUT` troca o tipo existente e `DELETE` e idempotente. Os tipos disponiveis sao `LIKE`, `HELPFUL`, `CELEBRATE` e `INSIGHTFUL`.
Bloqueios impedem comentarios e reacoes entre os dois usuarios. Silenciamentos afetam apenas o feed de quem silenciou. Desbloqueio e dessilenciamento sao idempotentes.

### Admin

| Metodo | Endpoint | Auth | Role |
|---|---|---|---|
| GET | `/admin/courses` | Sim | `ADMIN` |
| GET | `/admin/courses/all` | Sim | `ADMIN` |
| GET | `/admin/courses/{id}` | Sim | `ADMIN` |
| PATCH | `/admin/courses/{id}/archive` | Sim | `ADMIN` |
| PATCH | `/admin/courses/{id}/restore` | Sim | `ADMIN` |
| DELETE | `/admin/courses/{id}` | Sim | `ADMIN` |
| GET | `/admin/comments` | Sim | `ADMIN` |
| GET | `/admin/comments/all` | Sim | `ADMIN` |
| PATCH | `/admin/comments/{id}/hide` | Sim | `ADMIN` |
| PATCH | `/admin/comments/{id}/restore` | Sim | `ADMIN` |
| GET | `/admin/users` | Sim | `ADMIN` |
| GET | `/admin/users/all` | Sim | `ADMIN` |
| PATCH | `/admin/users/{id}/role` | Sim | `ADMIN` |
| GET | `/admin/comment/lesson/retained` | Sim | `ADMIN` |
| POST | `/admin/comment/lesson/{commentId}/moderation` | Sim | `ADMIN` |
| DELETE | `/admin/comment/lesson/{commentId}` | Sim | `ADMIN` |

## Respostas de Erro

O backend padroniza erros com `ApiError`.

Status tratados explicitamente:

- `400 Bad Request`
- `401 Unauthorized`
- `403 Forbidden`
- `404 Not Found`
- `409 Conflict`
- `500 Internal Server Error`

Exemplo:

```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "Only teachers can manage courses.",
  "path": "/course"
}
```

