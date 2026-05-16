package de.haushaltsbuch.dto;

import java.math.BigDecimal;
import java.util.List;

public record MonatsbuchungVorschauDTO(
        List<VorschauPostenDTO> posten,
        BigDecimal gesamtbetrag
) {}
