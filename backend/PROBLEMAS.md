# Problemas conhecidos

## Colisão de nomes de beans entre contextos

### Cenário

O projeto organiza funcionalidades por contexto e permite que classes de packages diferentes tenham o mesmo nome simples. Exemplos:

- `com.devnest.course.service.course.CourseService`;
- `com.devnest.admin.service.course.CourseService`;
- `com.devnest.course.controller.student.CommentController`;
- `com.devnest.admin.controller.comment.CommentController`.

Essa repetição é intencional: cada classe representa uma responsabilidade diferente dentro do próprio contexto.

### Comportamento do Spring

Por padrão, o Spring gera o nome do bean a partir do nome simples da classe, iniciando com letra minúscula. Assim, duas classes chamadas `CourseService`, mesmo em packages diferentes, tentam registrar o bean `courseService`.

O package diferencia as classes no Java, mas não faz parte do nome padrão do bean. Quando os dois componentes entram no mesmo Application Context, pode ocorrer uma colisão de definição de bean durante a inicialização. Testes que carregam um recorte ou uma combinação diferente do contexto também podem revelar beans ausentes, duplicados ou ambíguos.

### Solução adotada

Manter nomes de classes iguais quando fizer sentido dentro dos contextos e atribuir um nome explícito aos beans administrativos:

```java
@Service("adminCourseService")
public class CourseService {
}

@RestController("adminCourseController")
public class CourseController {
}
```

O mesmo padrão já é usado em beans como:

- `adminAccessService`;
- `adminUserService`;
- `adminMetricsService`;
- `adminCommentService`;
- `adminCourseService`;
- `adminCommentController`;
- `adminCourseController`.

Ao criar uma classe administrativa com o mesmo nome simples de uma classe de outro contexto, o bean de `admin` deve receber o prefixo `admin` explicitamente.

### Atenção nos testes

Ao refatorar classes para nomes iguais, revisar testes com contexto Spring, mocks e injeções. Dependendo do tipo de teste, pode ser necessário:

- mockar o bean pelo tipo correto;
- informar o nome do bean quando houver mais de um candidato compatível;
- importar somente os controllers e services necessários no teste de recorte;
- garantir que dependências de filtros e configurações de segurança também estejam disponíveis ou mockadas.

### Falha atualmente observada na suíte

O erro atual de `AdminManagementServiceTests.adminCanSearchArchiveRestoreAndDeleteCourses` não é uma colisão de beans. O contexto inicia e o teste chega a executar `CourseService.delete`, mas recebe `ForbiddenException` porque o service chamado valida a role `TEACHER`.

Essa falha de comportamento será discutida separadamente. Ela não invalida o problema arquitetural de nomes de beans descrito acima.
