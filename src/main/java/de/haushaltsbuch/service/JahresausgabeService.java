package de.haushaltsbuch.service;

import de.haushaltsbuch.dto.JahresausgabeDTO;
import de.haushaltsbuch.dto.JahresausgabeFormDTO;

import java.math.BigDecimal;
import java.util.List;

public interface JahresausgabeService {

    List<JahresausgabeDTO> findAllByUser(Long userId);

    JahresausgabeDTO create(JahresausgabeFormDTO form, Long userId);

    JahresausgabeDTO update(Long id, JahresausgabeFormDTO form, Long userId);

    void delete(Long id, Long userId);

    BigDecimal berechneGesamtMonatlicheRuecklage(Long userId);
}
