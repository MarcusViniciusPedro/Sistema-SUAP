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

    /**
     * O agrupamento por categoria deve refletir as contagens reais, nao apenas somar ao total.
     * Com duas linhas "Documentos" e uma "Matrícula", o resultado precisa trazer Documentos=2 e
     * Matrícula=1, e, como a ordenacao e por contagem desc e depois rotulo asc, "Documentos" vem
     * antes de "Matrícula". A soma continua batendo com o total como verificacao complementar.
     */
    @Test void agruparPorCategoriaContaEOrdenaCorretamente(){
        // Banco H2 em memoria e compartilhado entre os testes; limpa antes de semear para isolar.
        repository.deleteAll();
        repository.save(novoChamado("Documentos","BAIXA","ABERTO"));
        repository.save(novoChamado("Documentos","MEDIA","ABERTO"));
        repository.save(novoChamado("Matrícula","ALTA","CONCLUIDO"));
        IndicadoresDTO ind=consultas.indicadores();
        List<ContagemRotulada> porCategoria=ind.porCategoria();

        // Contagens reais por rotulo: quebraria se a chave de agrupamento estivesse errada.
        long documentos=valorDaCategoria(porCategoria,"Documentos");
        long matricula=valorDaCategoria(porCategoria,"Matrícula");
        assertEquals(2, documentos, "Documentos deve ter duas ocorrencias");
        assertEquals(1, matricula, "Matrícula deve ter uma ocorrencia");

        // Ordem estavel: maior contagem primeiro; quebraria se a ordenacao fosse removida.
        List<String> rotulos=porCategoria.stream().map(ContagemRotulada::rotulo).toList();
        assertTrue(rotulos.indexOf("Documentos") < rotulos.indexOf("Matrícula"),
                "Documentos (2) deve aparecer antes de Matrícula (1)");

        // Verificacao complementar: a particao continua somando ao total.
        long somaCategorias=porCategoria.stream().mapToLong(ContagemRotulada::valor).sum();
        assertEquals(ind.total(), somaCategorias);
    }

    /**
     * Categoria em branco deve cair no balde "Sem categoria", nao sumir do agrupamento.
     * A coluna categoria e NOT NULL no banco, entao o caso nulo nao e persistivel; aqui cobrimos
     * o caminho em branco (vazio e espacos), que exercita a mesma regra de isBlank() do agrupamento.
     */
    @Test void categoriaEmBrancoVaiParaSemCategoria(){
        // Banco H2 em memoria e compartilhado entre os testes; limpa antes de semear para isolar.
        repository.deleteAll();
        repository.save(novoChamado("","BAIXA","ABERTO"));
        repository.save(novoChamado("   ","MEDIA","ABERTO"));
        repository.save(novoChamado("Documentos","ALTA","CONCLUIDO"));
        IndicadoresDTO ind=consultas.indicadores();
        List<ContagemRotulada> porCategoria=ind.porCategoria();

        // Uma linha vazia e uma so com espacos somam duas ocorrencias no balde "Sem categoria".
        assertEquals(2, valorDaCategoria(porCategoria,"Sem categoria"),
                "Categoria vazia e em branco devem ser agrupadas como 'Sem categoria'");
        assertEquals(1, valorDaCategoria(porCategoria,"Documentos"));
    }

    /** Devolve o valor agrupado de um rotulo ou falha o teste se o rotulo nao existir. */
    private long valorDaCategoria(List<ContagemRotulada> porCategoria,String rotulo){
        return porCategoria.stream()
                .filter(c -> rotulo.equals(c.rotulo()))
                .mapToLong(ContagemRotulada::valor)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Categoria ausente no agrupamento: " + rotulo));
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
