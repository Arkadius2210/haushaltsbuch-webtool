package de.haushaltsbuch.dto;

public record ImportFehlerDTO(
        String arbeitsblatt,
        int zeile,
        String fehlermeldung
) {}
