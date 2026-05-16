package de.haushaltsbuch.dto;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.NotNull;

public record MonatsbuchungFormDTO(
        @NotNull LocalDate datum,
        List<VorschauPostenDTO> posten
) {}
