package de.haushaltsbuch.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record UmbuchungFormDTO(
        @NotNull Long quellkontoId,
        @NotNull Long zielkontoId,
        @NotNull @Positive BigDecimal betrag,
        @NotNull LocalDate datum,
        @Size(max = 255) String beschreibung
) {}
