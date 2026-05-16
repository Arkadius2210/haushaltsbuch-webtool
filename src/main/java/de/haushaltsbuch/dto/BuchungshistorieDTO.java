package de.haushaltsbuch.dto;

import java.math.BigDecimal;
import java.util.List;

public record BuchungshistorieDTO(
        Long sparkontoId,
        String sparkontoName,
        List<BuchungDTO> buchungen,
        BigDecimal aktuellerSaldo
) {}
