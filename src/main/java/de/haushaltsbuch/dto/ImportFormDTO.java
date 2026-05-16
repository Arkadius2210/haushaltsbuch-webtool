package de.haushaltsbuch.dto;

import java.util.List;

public record ImportFormDTO(
        List<SparkontoFormDTO> sparkonten,
        List<JahresausgabeFormDTO> jahresausgaben
) {}
