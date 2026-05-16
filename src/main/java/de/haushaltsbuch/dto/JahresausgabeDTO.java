package de.haushaltsbuch.dto;

import java.math.BigDecimal;

public record JahresausgabeDTO(
        Long id,
        String name,
        BigDecimal jahresbetrag,
        BigDecimal monatlicheRuecklage
) {}
