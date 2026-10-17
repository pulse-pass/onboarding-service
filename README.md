# onboarding-service

Serviço de onboarding do PulsePass: cadastro das pessoas e da equipe de cada empresa organizadora. É o único serviço que fala com o Keycloak.

## Subir localmente

Para subir o serviço só é necessário o Docker: a imagem compila a aplicação com o JDK que ela mesma traz. O Java 21 só é necessário para rodar o `./mvnw` (lint e testes) fora do container.

```bash
./infrastructure/service-start.sh [test|development]
./infrastructure/service-stop.sh
```

O script cria a rede `pulse-pass` e o `infrastructure/.env` (a partir do `.env.example`) quando não existem, derruba o que estiver no ar e sobe o serviço. O modo padrão é `test`, em que as dependências externas (Keycloak e `organization-service`) são atendidas pelo MockServer. No modo `development` elas apontam para o Keycloak do `pulse-pass-infrastructure` e para o `organization-service` real, que precisa estar no ar na rede `pulse-pass`. Para cada par `<NAME>_TEST_URL` / `<NAME>_DEVELOPMENT_URL` do `.env`, o script exporta `<NAME>_URL` conforme o modo, então novas dependências externas não exigem mudar o script. O `service-stop.sh` remove containers, volumes e imagens locais.

A API fica em `http://localhost:8081` (`API_PORT`), o MySQL em `localhost:3307` e, no modo `test`, o MockServer em `localhost:1081`.

## Rotas

`POST /onboarding/legal_persons` (pública) faz o autocadastro da empresa organizadora. Grava a pessoa jurídica, o dono da conta (pessoa física) e o vínculo `OWNER`, cria o usuário do dono no Keycloak (usuário = CPF) com o papel `organizer` e pede ao `organization-service` a organização `PENDING`.

```json
{
  "document_number": "11222333000181",
  "name": "Lenha Produções Artísticas Ltda",
  "email": "contato@lenha.com.br",
  "phone_number": { "country_code": "55", "area_code": "51", "number": "30224180" },
  "legal_person_collaborators": [
    {
      "collaborator_role": "OWNER",
      "document_number": "52998224725",
      "name": "Bianca Torres",
      "email": "bianca@lenha.com.br",
      "password": "12345678"
    }
  ]
}
```

Os documentos vão só com dígitos, a lista de colaboradores tem exatamente um item e o papel deve ser `OWNER`. A resposta `201` traz apenas a pessoa jurídica (`person_key`, `document_number`, `name`, `email`), sem o dono nem o telefone. Se o Keycloak ou o `organization-service` falharem depois da gravação, a resposta é 422 (`ONB00006` ou `ONB00007`, com a mensagem para tentar novamente) e o erro vai para o log; o cadastro gravado não é desfeito, e repetir o mesmo CNPJ devolve `ONB00001`.

Erros, no formato `{ "title", "message", "code" }` (mensagem em inglês ou português, conforme `Accept-Language`):

| Código | Status | Situação |
| --- | --- | --- |
| `GEN00400` | 400 | Corpo inválido: campo ausente, formato ou tamanho incorreto |
| `GEN00404` | 404 | Rota inexistente |
| `GEN00405` | 405 | Método não permitido na rota |
| `GEN00415` | 415 | Tipo de conteúdo não suportado |
| `GEN00500` | 500 | Erro interno inesperado |
| `ONB00001` | 409 | CNPJ já cadastrado |
| `ONB00002` | 409 | CPF do dono já cadastrado |
| `ONB00003` | 400 | Dígitos verificadores do CNPJ inválidos |
| `ONB00004` | 400 | Dígitos verificadores do CPF inválidos |
| `ONB00005` | 400 | Papel do colaborador diferente de `OWNER` |
| `ONB00006` | 422 | Falha ao criar o usuário no Keycloak |
| `ONB00007` | 422 | Falha ao criar a organização no `organization-service` |

A saúde do serviço fica em `/actuator/health`.

## Lint e testes

Os testes de integração dependem do MockServer, então o compose precisa estar no ar com `./infrastructure/service-start.sh test`.

```bash
./mvnw checkstyle:check        # Checkstyle
./mvnw test                    # testes unitários (relatório JaCoCo em target/site/jacoco)
./mvnw test -Pintegration      # testes de integração (exigem o compose no ar em modo test)
```
