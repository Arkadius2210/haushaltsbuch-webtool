package de.haushaltsbuch.dto;

import java.util.List;

public record ImportVorschauDTO(
        List<SparkontoFormDTO> sparkonten,
        List<JahresausgabeFormDTO> jahresausgaben,
        List<ImportFehlerDTO> fehler
) {}
