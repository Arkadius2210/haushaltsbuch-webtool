package de.haushaltsbuch.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SparkontoFormDTO(
        @NotBlank @Size(max = 100) String name,
        @NotNull BigDecimal standardbetrag
) {}
