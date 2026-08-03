# Processo de implementação

> Ordem obrigatória para implementar cada incremento da comunidade do DevNest.

As etapas abaixo devem ser executadas em ordem estrita. Uma etapa somente começa quando a anterior estiver concluída e revisada.

## 1. Modelagem

- definir entidades, relacionamentos, cardinalidades e responsabilidades do domínio;
- definir enums, estados e transições permitidas;
- estabelecer constraints, chaves, índices e estratégia de exclusão lógica;
- registrar regras de negócio, decisões pendentes e impactos de segurança;
- revisar a modelagem antes de criar testes ou iniciar a implementação.

## 2. Testes

- definir os comportamentos esperados e os critérios de aceite;
- criar primeiro os testes dos casos felizes, erros, autorização e regras de negócio;
- incluir casos de limite e concorrência quando aplicáveis;
- confirmar que os novos testes falham pela ausência do comportamento esperado;
- não alterar os testes apenas para acomodar uma implementação incorreta.

## 3. Services e repositories

- implementar as entidades e enums mínimos necessários para os testes compilarem;
- criar repositories com as operações básicas de persistência;
- implementar regras de negócio, transações, ownership e autorização nos services;
- manter controllers fora desta etapa;
- executar os testes e corrigir a implementação antes de avançar.

## 4. Queries

- implementar consultas derivadas ou explícitas necessárias para o incremento;
- garantir paginação e ordenação determinística;
- evitar N+1 e carregamento desnecessário de coleções;
- criar índices e constraints de banco coerentes com as consultas;
- testar filtros, isolamento de dados, concorrência e performance relevante.

## 5. DTOs

- criar DTOs distintos para entrada e saída;
- aplicar Bean Validation nos contratos de entrada;
- não expor entities JPA diretamente;
- representar paginação, enums, datas e erros de maneira consistente com o restante da API;
- manter nos DTOs somente os campos necessários ao contrato público.

## 6. Endpoints e mappers

- criar mappers entre DTOs e modelos do domínio;
- expor os endpoints somente depois das regras e consultas estarem testadas;
- proteger as rotas com autenticação e autorização adequadas;
- delegar regras de negócio aos services;
- adicionar testes de controller para status HTTP, validação, segurança e formato das respostas;
- executar a suíte completa antes de considerar o incremento concluído.

## Regra de conclusão

Um incremento só está pronto quando todas as seis etapas foram concluídas nessa ordem, os testes passam e nenhuma entity é exposta diretamente pela API.
