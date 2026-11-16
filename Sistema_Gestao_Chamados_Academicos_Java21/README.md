# Sistema de Gestão de Chamados Acadêmicos — LP2 / IFSP

Projeto educacional integrado, desenvolvido a partir do layout de `projeto-dto-status-chamados` fornecido pelo docente. Mantém fundo claro, Arial, tabela de cabeçalho verde e páginas/formulários simples.

## Tecnologias e conceitos

Java 21; Maven; Spring Boot 3.3.5; orientação a objetos (entidades, métodos, encapsulamento, classes e injeção de dependências); MVC (Controller, Service, Repository, Thymeleaf); CRUD; JPA e H2 persistido em arquivo; DTOs; Factory; API REST JSON; login e sessão HTTP; filtros dinâmicos por Specifications; indicadores; exportações CSV, XLSX (Apache POI) e PDF (PDFBox).

## Como executar no IntelliJ

1. Certifique-se de ter **JDK 21 instalado** e acessível ao IntelliJ; isso depende do computador.
2. Extraia o ZIP. Escolha **File > Open** e selecione o **pom.xml** (abrir como projeto Maven).
3. Espere o Maven baixar as dependências (exige internet na primeira importação).
4. Execute a classe `br.edu.ifsp.lgp2.ProjetoDtoStatusChamadosApplication` no pacote de código.
5. Abra `http://localhost:8080/login`.

**Login exclusivamente didático:** usuário `admin`, senha `admin123`. Alterar via variáveis de ambiente `DEMO_USUARIO` e `DEMO_SENHA`. Não é uma solução de autenticação para produção (não há usuários persistidos, hashing de senhas, RBAC ou CSRF).

Banco H2 persistente criado automaticamente em `./dados/` na pasta de execução (dados não são apagados ao reiniciar). A carga inicial de 3 chamados acontece somente com banco vazio. Não é necessário configurar o H2 para usar o projeto.

## Telas

- `/chamados`: listagem, filtros combinados, formulários CRUD e alteração de status via DTO.
- `/indicadores`: totais e percentuais em painel.
- `/relatorios`: consulta, totais do recorte e download dos resultados filtrados em PDF, CSV e Excel.
- `/login` e `/logout`: autenticação demonstrativa em sessão.

## API (sessão obrigatória)

Faça login no navegador; para ferramentas como Postman, use o cookie `JSESSIONID` obtido após POST `/login` com formulário `usuario` e `senha`.

- `GET /api/chamados?status=ABERTO&prioridade=ALTA`
- `GET /api/chamados/{id}`
- `GET /api/chamados/indicadores`
- `POST /api/chamados` corpo JSON `{"titulo":"Exemplo","descricao":"Solicitacao","categoria":"Documentos"}`
- `PUT /api/chamados/{id}/status` corpo JSON `{"status":"CONCLUIDO","observacaoAtendimento":"Resolvido"}`
- `DELETE /api/chamados/{id}`

## Organização didática

`model` persiste dados; `repository` consulta via Spring Data JPA; `dto` recebe parâmetros; `factory` preenche valores iniciais; `service` concentra regras; `controller` recebe HTTP e escolhe HTML/JSON/download; `config` inicializa exemplos e protege rotas por sessão.

## Compilação e teste

No terminal da pasta que contém `pom.xml`: `mvn clean verify`. Para rodar sem IntelliJ: `mvn spring-boot:run`.

**Limitação da validação desta entrega:** o ambiente utilizado para gerar o ZIP possui Java 21, mas não possui Maven instalado nem acesso à internet para obter as dependências. A compilação completa não pôde ser executada aqui. Não há arquivos `.idea`/`.iml` no ZIP para evitar SDKs locais inválidos.
