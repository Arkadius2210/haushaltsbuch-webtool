package de.haushaltsbuch.service;

import de.haushaltsbuch.dto.SparkontoDTO;
import de.haushaltsbuch.dto.SparkontoFormDTO;

import java.math.BigDecimal;
import java.util.List;

public interface SparkontoService {

    List<SparkontoDTO> findAllByUser(Long userId);

    SparkontoDTO create(SparkontoFormDTO form, Long userId);

    SparkontoDTO update(Long id, SparkontoFormDTO form, Long userId);

    void delete(Long id, Long userId);

    BigDecimal berechneGesamtsaldo(Long userId);

    BigDecimal berechneMonatlicherGesamtbedarf(Long userId);
}
