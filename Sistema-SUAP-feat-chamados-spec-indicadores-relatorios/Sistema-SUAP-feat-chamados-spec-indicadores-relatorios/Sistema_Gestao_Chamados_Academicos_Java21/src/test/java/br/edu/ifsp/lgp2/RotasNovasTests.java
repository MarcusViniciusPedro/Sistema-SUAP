package br.edu.ifsp.lgp2;

import br.edu.ifsp.lgp2.model.Chamado;
import br.edu.ifsp.lgp2.repository.ChamadoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes MockMvc das rotas adicionadas: PUT completo em /api/chamados/{id}
 * (sucesso, 404, corpo
 * parcial e status invalido) e a tela /relatorios/preview mais o
 * redirecionamento de /relatorios.
 * O SessaoInterceptor exige sessao autenticada, entao usamos
 * sessionAttr("usuario", ...).
 */
@SpringBootTest(properties = { "spring.datasource.url=jdbc:h2:mem:rotas;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop" })
@AutoConfigureMockMvc
class RotasNovasTests {

    @Autowired
    MockMvc mvc;
    @Autowired
    ChamadoRepository repository;

    private Long idSemente() {
        return repository.findAll().get(0).getId();
    }

    private String corpoCompleto() {
        return "{\"titulo\":\"Projetor queimado\",\"descricao\":\"Sala 12 sem imagem\","
                + "\"categoria\":\"Suporte técnico\",\"status\":\"EM_ANALISE\","
                + "\"prioridade\":\"ALTA\",\"responsavel\":\"Equipe de TI\"}";
    }

    @Test
    void putCompletoAtualizaEDevolve200() throws Exception {
        mvc.perform(put("/api/chamados/" + idSemente()).sessionAttr("usuario", "admin")
                .contentType(MediaType.APPLICATION_JSON).content(corpoCompleto()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_ANALISE"))
                .andExpect(jsonPath("$.titulo").value("Projetor queimado"));
    }

    @Test
    void putIdInexistenteDevolve404() throws Exception {
        mvc.perform(put("/api/chamados/999999").sessionAttr("usuario", "admin")
                .contentType(MediaType.APPLICATION_JSON).content(corpoCompleto()))
                .andExpect(status().isNotFound());
    }

    @Test
    void putCorpoParcialDevolve400() throws Exception {
        String parcial = "{\"titulo\":\"So titulo e descricao\",\"descricao\":\"faltam os demais\"}";
        mvc.perform(put("/api/chamados/" + idSemente()).sessionAttr("usuario", "admin")
                .contentType(MediaType.APPLICATION_JSON).content(parcial))
                .andExpect(status().isBadRequest());
    }

    @Test
    void putStatusInvalidoDevolve400() throws Exception {
        String invalido = "{\"titulo\":\"T\",\"descricao\":\"D\",\"categoria\":\"Documentos\","
                + "\"status\":\"INVENTADO\",\"prioridade\":\"ALTA\",\"responsavel\":\"R\"}";
        mvc.perform(put("/api/chamados/" + idSemente()).sessionAttr("usuario", "admin")
                .contentType(MediaType.APPLICATION_JSON).content(invalido))
                .andExpect(status().isBadRequest());
    }

    @Test
    void previewRespondeComATela() throws Exception {
        mvc.perform(get("/relatorios/preview").sessionAttr("usuario", "admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("relatorios-preview"))
                .andExpect(model().attributeExists("filtro", "chamados", "indicadores"));
    }

    @Test
    void relatoriosRedirecionaParaPreview() throws Exception {
        mvc.perform(get("/relatorios").param("status", "ABERTO").sessionAttr("usuario", "admin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/relatorios/preview?status=ABERTO"));
    }
}
