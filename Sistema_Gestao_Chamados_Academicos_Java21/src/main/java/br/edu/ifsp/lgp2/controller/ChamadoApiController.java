package br.edu.ifsp.lgp2.controller;

import br.edu.ifsp.lgp2.dto.*;
import br.edu.ifsp.lgp2.model.Chamado;
import br.edu.ifsp.lgp2.service.ChamadoService;
import br.edu.ifsp.lgp2.service.ConsultaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Set;

/** API REST: responde JSON em vez de telas HTML; a mesma camada Service é reutilizada. */
@RestController
@RequestMapping("/api/chamados")
public class ChamadoApiController {
    private final ChamadoService chamados;
    private final ConsultaService consultas;
    public ChamadoApiController(ChamadoService chamados,ConsultaService consultas){this.chamados=chamados;this.consultas=consultas;}
    @GetMapping public List<Chamado> listar(@ModelAttribute FiltroChamadoDTO filtro){return consultas.consultar(filtro);}
    @GetMapping("/{id}") public ResponseEntity<Chamado> buscar(@PathVariable Long id){return ResponseEntity.of(chamados.buscarOpcional(id));}
    @GetMapping("/indicadores") public IndicadoresDTO indicadores(){return consultas.indicadores();}
    @PostMapping public ResponseEntity<Chamado> criar(@RequestBody ChamadoCadastroDTO dto){
        if(dto.getTitulo()==null || dto.getTitulo().isBlank())return ResponseEntity.badRequest().build();
        return ResponseEntity.status(HttpStatus.CREATED).body(chamados.salvarNovoChamado(dto));
    }
    // Conjuntos de valores aceitos para status e prioridade (mesmos usados nos fluxos MVC).
    private static final Set<String> STATUS=Set.of("ABERTO","EM_ANALISE","CONCLUIDO","CANCELADO");
    private static final Set<String> PRIORIDADES=Set.of("BAIXA","MEDIA","ALTA","URGENTE");
    /**
     * Atualizacao completa via REST: reusa o Service e devolve 404 quando o id nao existe.
     * A atualizacao e completa, entao o corpo precisa trazer todos os campos obrigatorios; um corpo
     * parcial geraria null em colunas nullable=false. Validamos antes de persistir e devolvemos 400
     * para corpo incompleto ou com status/prioridade fora dos valores aceitos (contrato fail-closed).
     */
    @PutMapping("/{id}") public ResponseEntity<Chamado> atualizar(@PathVariable Long id,@RequestBody Chamado corpo){
        if(!completoEValido(corpo))return ResponseEntity.badRequest().build();
        corpo.setId(id);return ResponseEntity.of(java.util.Optional.ofNullable(chamados.atualizarChamado(corpo)));
    }
    /** Garante que os campos obrigatorios vieram preenchidos e que status/prioridade sao valores conhecidos. */
    private boolean completoEValido(Chamado c){
        return preenchido(c.getTitulo()) && preenchido(c.getDescricao()) && preenchido(c.getCategoria())
                && preenchido(c.getResponsavel())
                && c.getStatus()!=null && STATUS.contains(c.getStatus())
                && c.getPrioridade()!=null && PRIORIDADES.contains(c.getPrioridade());
    }
    private boolean preenchido(String valor){return valor!=null && !valor.isBlank();}
    @PutMapping("/{id}/status") public ResponseEntity<Chamado> status(@PathVariable Long id,@RequestBody ChamadoAtualizacaoStatusDTO dto){
        dto.setId(id);return ResponseEntity.of(java.util.Optional.ofNullable(chamados.atualizarStatus(dto)));
    }
    @DeleteMapping("/{id}") public ResponseEntity<Void> excluir(@PathVariable Long id){
        if(!chamados.existe(id))return ResponseEntity.notFound().build();
        chamados.excluir(id);return ResponseEntity.noContent().build();
    }
}
