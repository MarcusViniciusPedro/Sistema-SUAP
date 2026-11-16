package br.edu.ifsp.lgp2.controller;

import br.edu.ifsp.lgp2.dto.FiltroChamadoDTO;
import br.edu.ifsp.lgp2.service.ConsultaService;
import br.edu.ifsp.lgp2.service.ExportacaoService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import java.io.IOException;

/** Controller MVC: apresenta indicadores e devolve arquivos como resposta HTTP. */
@Controller
public class RelatorioController {
    private final ConsultaService consulta;
    private final ExportacaoService exportacao;
    public RelatorioController(ConsultaService consulta,ExportacaoService exportacao) {
        this.consulta=consulta;this.exportacao=exportacao;
    }
    @GetMapping("/indicadores")
    public String indicadores(Model model) {
        model.addAttribute("indicadores",consulta.indicadores());
        return "indicadores";
    }
    // A tela de relatorios foi substituida pela pre-visualizacao. Mantemos a rota antiga apenas
    // como redirecionamento para /relatorios/preview, preservando os filtros da query string, para
    // nao deixar uma rota orfa acessivel somente digitando a URL.
    @GetMapping("/relatorios")
    public String relatorios(@ModelAttribute FiltroChamadoDTO filtro) {
        var url=UriComponentsBuilder.fromPath("/relatorios/preview");
        if(preenchido(filtro.getBusca()))url.queryParam("busca",filtro.getBusca());
        if(preenchido(filtro.getStatus()))url.queryParam("status",filtro.getStatus());
        if(preenchido(filtro.getCategoria()))url.queryParam("categoria",filtro.getCategoria());
        if(preenchido(filtro.getPrioridade()))url.queryParam("prioridade",filtro.getPrioridade());
        if(filtro.getInicio()!=null)url.queryParam("inicio",filtro.getInicio());
        if(filtro.getFim()!=null)url.queryParam("fim",filtro.getFim());
        return "redirect:"+url.build().encode().toUriString();
    }
    private boolean preenchido(String valor){return valor!=null && !valor.isBlank();}
    // Tela de pre-visualizacao do relatorio: mostra o formulario de filtros, a previa dos chamados
    // filtrados, os totais do recorte e os botoes de download (CSV/Excel/PDF). Reutiliza os mesmos
    // servicos de consulta e indicadores; os downloads continuam usando /relatorios/exportar/{formato}.
    @GetMapping("/relatorios/preview")
    public String preview(@ModelAttribute FiltroChamadoDTO filtro,Model model) {
        model.addAttribute("filtro",filtro);
        model.addAttribute("chamados",consulta.consultar(filtro));
        model.addAttribute("indicadores",consulta.indicadores(filtro));
        return "relatorios-preview";
    }
    @GetMapping("/relatorios/exportar/{formato}")
    public ResponseEntity<byte[]> exportar(@PathVariable String formato,@ModelAttribute FiltroChamadoDTO filtro) throws IOException {
        var registros=consulta.consultar(filtro);
        byte[] conteudo;
        String tipo;
        String extensao;
        switch(formato.toLowerCase()) {
            case "csv" -> { conteudo=exportacao.csv(registros);tipo="text/csv;charset=UTF-8";extensao="csv"; }
            case "excel" -> { conteudo=exportacao.excel(registros);tipo="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";extensao="xlsx"; }
            case "pdf" -> { conteudo=exportacao.pdf(registros);tipo="application/pdf";extensao="pdf"; }
            default -> { return ResponseEntity.badRequest().body(new byte[0]); }
        }
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=chamados."+extensao)
                .contentType(MediaType.parseMediaType(tipo)).body(conteudo);
    }
}
