package br.edu.ifsp.lgp2;

import br.edu.ifsp.lgp2.dto.ContagemRotulada;
import br.edu.ifsp.lgp2.dto.IndicadoresDTO;
import br.edu.ifsp.lgp2.model.Chamado;
import br.edu.ifsp.lgp2.service.ConsultaService;
import br.edu.ifsp.lgp2.repository.ChamadoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Teste de integração: o contexto Spring carrega e as consultas JPA funcionam. */
@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:teste;DB_CLOSE_DELAY=-1","spring.jpa.hibernate.ddl-auto=create-drop"})
class ProjetoApplicationTests {
    @Autowired ConsultaService consultas;
    @Autowired ChamadoRepository repository;
    @Test void indicadoresSaoCoerentes(){assertEquals(repository.count(),consultas.indicadores().total());}

    /** As agregacoes por prioridade trazem sempre os quatro valores conhecidos, na ordem fixa. */
    @Test void prioridadeTemOsQuatroValoresConhecidos(){
        List<ContagemRotulada> porPrioridade=consultas.indicadores().porPrioridade();
        assertEquals(4, porPrioridade.size());
        assertEquals(List.of("BAIXA","MEDIA","ALTA","URGENTE"),
                porPrioridade.stream().map(ContagemRotulada::rotulo).toList());
    }

    /** A soma das categorias agrupadas deve bater com o total de chamados. */
    @Test void somaDasCategoriasBateComTotal(){
        repository.save(novoChamado("Documentos","BAIXA","ABERTO"));
        repository.save(novoChamado("Documentos","MEDIA","ABERTO"));
        repository.save(novoChamado("Matrícula","ALTA","CONCLUIDO"));
        IndicadoresDTO ind=consultas.indicadores();
        long somaCategorias=ind.porCategoria().stream().mapToLong(ContagemRotulada::valor).sum();
        assertEquals(ind.total(), somaCategorias);
    }

    private Chamado novoChamado(String categoria,String prioridade,String status){
        Chamado c=new Chamado();
        c.setTitulo("Titulo teste");
        c.setDescricao("Descricao teste");
        c.setCategoria(categoria);
        c.setPrioridade(prioridade);
        c.setStatus(status);
        c.setResponsavel("Responsavel teste");
        return c;
    }
}
