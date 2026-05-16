package de.haushaltsbuch.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record BuchungDTO(
        Long id,
        Long sparkontoId,
        String sparkontoName,
        LocalDate datum,
        BigDecimal betrag,
        String typ,
        String beschreibung,
        LocalDateTime erstelltAm,
        BigDecimal laufenderKontostand
) {}
