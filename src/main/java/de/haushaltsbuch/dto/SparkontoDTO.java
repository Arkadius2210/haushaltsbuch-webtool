package de.haushaltsbuch.dto;

import java.math.BigDecimal;

public record SparkontoDTO(
        Long id,
        String name,
        BigDecimal standardbetrag,
        BigDecimal kontostand
) {}
