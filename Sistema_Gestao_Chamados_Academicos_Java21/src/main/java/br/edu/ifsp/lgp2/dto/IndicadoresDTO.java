package br.edu.ifsp.lgp2.dto;

/** Record do Java 21: objeto imutável para transportar indicadores calculados. */
public record IndicadoresDTO(long total, long abertos, long emAnalise, long concluidos,
                            long cancelados, long altaPrioridade, double percentualConcluido) {}
