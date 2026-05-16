package de.haushaltsbuch.dto;

import java.math.BigDecimal;
import java.util.List;

public record BuchungErgebnisDTO(
        BigDecimal gesamtsaldoVorher,
        BigDecimal gesamtsaldoNachher,
        BigDecimal saldoDifferenz,
        List<BuchungDTO> gebuchtePosten
) {}
