package br.edu.ifsp.lgp2.dto;

import java.util.List;

/**
 * Record do Java 21: objeto imutável para transportar indicadores calculados.
 */
public record IndicadoresDTO(long total, long abertos, long emAnalise, long concluidos,
                long cancelados, long altaPrioridade, double percentualConcluido,
                List<ContagemRotulada> porCategoria, List<ContagemRotulada> porPrioridade) {
}
