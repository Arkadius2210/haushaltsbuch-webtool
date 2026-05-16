package de.haushaltsbuch.service;

import de.haushaltsbuch.dto.JahresausgabeDTO;
import de.haushaltsbuch.dto.JahresausgabeFormDTO;
import de.haushaltsbuch.exception.ZugriffVerweigertException;
import de.haushaltsbuch.model.Benutzer;
import de.haushaltsbuch.model.Jahresausgabe;
import de.haushaltsbuch.repository.JahresausgabeRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class JahresausgabeServiceImpl implements JahresausgabeService {

    private final JahresausgabeRepository jahresausgabeRepository;

    public JahresausgabeServiceImpl(JahresausgabeRepository jahresausgabeRepository) {
        this.jahresausgabeRepository = jahresausgabeRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<JahresausgabeDTO> findAllByUser(Long userId) {
        return jahresausgabeRepository.findAllByBenutzerId(userId).stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    public JahresausgabeDTO create(JahresausgabeFormDTO form, Long userId) {
        Benutzer benutzer = new Benutzer();
        benutzer.setId(userId);

        Jahresausgabe jahresausgabe = new Jahresausgabe();
        jahresausgabe.setName(form.name());
        jahresausgabe.setJahresbetrag(form.jahresbetrag());
        jahresausgabe.setBenutzer(benutzer);
        jahresausgabe.setErstelltAm(LocalDateTime.now());

        Jahresausgabe saved = jahresausgabeRepository.save(jahresausgabe);
        return toDTO(saved);
    }

    @Override
    public JahresausgabeDTO update(Long id, JahresausgabeFormDTO form, Long userId) {
        Jahresausgabe jahresausgabe = jahresausgabeRepository.findByIdAndBenutzerId(id, userId)
                .orElseThrow(() -> new ZugriffVerweigertException());

        jahresausgabe.setName(form.name());
        jahresausgabe.setJahresbetrag(form.jahresbetrag());

        Jahresausgabe saved = jahresausgabeRepository.save(jahresausgabe);
        return toDTO(saved);
    }

    @Override
    public void delete(Long id, Long userId) {
        Jahresausgabe jahresausgabe = jahresausgabeRepository.findByIdAndBenutzerId(id, userId)
                .orElseThrow(() -> new ZugriffVerweigertException());

        jahresausgabeRepository.delete(jahresausgabe);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal berechneGesamtMonatlicheRuecklage(Long userId) {
        return jahresausgabeRepository.findAllByBenutzerId(userId).stream()
                .map(j -> berechneMonatlicheRuecklage(j.getJahresbetrag()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal berechneMonatlicheRuecklage(BigDecimal jahresbetrag) {
        return jahresbetrag.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
    }

    private JahresausgabeDTO toDTO(Jahresausgabe jahresausgabe) {
        return new JahresausgabeDTO(
                jahresausgabe.getId(),
                jahresausgabe.getName(),
                jahresausgabe.getJahresbetrag(),
                berechneMonatlicheRuecklage(jahresausgabe.getJahresbetrag())
        );
    }
}
