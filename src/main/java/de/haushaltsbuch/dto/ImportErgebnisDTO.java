package de.haushaltsbuch.dto;

import java.util.List;

public record ImportErgebnisDTO(
        int sparkontenImportiert,
        int jahresausgabenImportiert,
        List<ImportFehlerDTO> fehler
) {}
