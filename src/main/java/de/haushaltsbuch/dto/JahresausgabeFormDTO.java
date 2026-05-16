package de.haushaltsbuch.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record JahresausgabeFormDTO(
        @NotBlank @Size(max = 100) String name,
        @NotNull @Positive BigDecimal jahresbetrag
) {}
