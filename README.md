Sistema-SUAP
O projeto final tem por finalidade consolidar os conteúdos desenvolvidos ao longo da disciplina por meio da evolução do Sistema de Gestão de Chamados Acadêmicos. A proposta é apresentar uma aplicação Spring Boot funcional, organizada em camadas, com persistência de dados, interface web, API REST e recursos de relatório e exportação.

Exemplos de entrada/saida da API REST
Os arquivos entrada.json e saida.json, em Sistema_Gestao_Chamados_Academicos_Java21/docs/api/, documentam o contrato da operação de criação de chamado via API REST.

entrada.json: corpo da requisição POST /api/chamados, com os campos do ChamadoCadastroDTO (titulo, descricao e categoria).
saida.json: resposta JSON do chamado criado, com os campos preenchidos pela ChamadoFactory (status ABERTO, prioridade e responsavel conforme a categoria, datas de abertura e atualizacao).
Como o formato JSON nao aceita comentarios, a explicacao dos arquivos fica registrada aqui.
