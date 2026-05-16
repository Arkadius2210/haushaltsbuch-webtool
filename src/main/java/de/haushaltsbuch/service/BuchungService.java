package de.haushaltsbuch.service;

import de.haushaltsbuch.dto.BuchungErgebnisDTO;
import de.haushaltsbuch.dto.BuchungshistorieDTO;
import de.haushaltsbuch.dto.ManuelleBuchungFormDTO;
import de.haushaltsbuch.dto.MonatsbuchungFormDTO;
import de.haushaltsbuch.dto.MonatsbuchungVorschauDTO;
import de.haushaltsbuch.dto.UmbuchungFormDTO;

import java.time.LocalDate;

public interface BuchungService {

    MonatsbuchungVorschauDTO vorschauErstellen(Long userId);

    BuchungErgebnisDTO monatsbuchungDurchfuehren(MonatsbuchungFormDTO form, Long userId);

    BuchungErgebnisDTO manuelleBuchungSpeichern(ManuelleBuchungFormDTO form, Long userId);

    BuchungErgebnisDTO umbuchungSpeichern(UmbuchungFormDTO form, Long userId);

    BuchungshistorieDTO getHistorie(Long sparkontoId, LocalDate von, LocalDate bis, Long userId);
}
