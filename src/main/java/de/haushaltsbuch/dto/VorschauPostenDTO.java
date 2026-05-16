package de.haushaltsbuch.dto;

import java.math.BigDecimal;

public record VorschauPostenDTO(
        Long sparkontoId,
        String sparkontoName,
        BigDecimal betrag,
        boolean aktiv
) {}
