package de.haushaltsbuch.service;

import de.haushaltsbuch.dto.SparkontoDTO;
import de.haushaltsbuch.dto.SparkontoFormDTO;
import de.haushaltsbuch.exception.ZugriffVerweigertException;
import de.haushaltsbuch.model.Benutzer;
import de.haushaltsbuch.model.Sparkonto;
import de.haushaltsbuch.repository.BuchungRepository;
import de.haushaltsbuch.repository.SparkontoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class SparkontoServiceImpl implements SparkontoService {

    private final SparkontoRepository sparkontoRepository;
    private final BuchungRepository buchungRepository;

    public SparkontoServiceImpl(SparkontoRepository sparkontoRepository,
                                BuchungRepository buchungRepository) {
        this.sparkontoRepository = sparkontoRepository;
        this.buchungRepository = buchungRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SparkontoDTO> findAllByUser(Long userId) {
        List<Sparkonto> konten = sparkontoRepository.findAllByBenutzerId(userId);
        return konten.stream()
                .map(konto -> new SparkontoDTO(
                        konto.getId(),
                        konto.getName(),
                        konto.getStandardbetrag(),
                        buchungRepository.berechneSaldo(konto.getId())
                ))
                .toList();
    }

    @Override
    public SparkontoDTO create(SparkontoFormDTO form, Long userId) {
        Benutzer benutzer = new Benutzer();
        benutzer.setId(userId);

        Sparkonto sparkonto = new Sparkonto();
        sparkonto.setName(form.name());
        sparkonto.setStandardbetrag(form.standardbetrag());
        sparkonto.setBenutzer(benutzer);
        sparkonto.setErstelltAm(LocalDateTime.now());

        Sparkonto saved = sparkontoRepository.save(sparkonto);

        return new SparkontoDTO(
                saved.getId(),
                saved.getName(),
                saved.getStandardbetrag(),
                BigDecimal.ZERO
        );
    }

    @Override
    public SparkontoDTO update(Long id, SparkontoFormDTO form, Long userId) {
        Sparkonto sparkonto = sparkontoRepository.findByIdAndBenutzerId(id, userId)
                .orElseThrow(() -> new ZugriffVerweigertException(
                        "Sparkonto nicht gefunden oder Zugriff verweigert"));

        sparkonto.setName(form.name());
        sparkonto.setStandardbetrag(form.standardbetrag());

        Sparkonto saved = sparkontoRepository.save(sparkonto);

        return new SparkontoDTO(
                saved.getId(),
                saved.getName(),
                saved.getStandardbetrag(),
                buchungRepository.berechneSaldo(saved.getId())
        );
    }

    @Override
    public void delete(Long id, Long userId) {
        Sparkonto sparkonto = sparkontoRepository.findByIdAndBenutzerId(id, userId)
                .orElseThrow(() -> new ZugriffVerweigertException(
                        "Sparkonto nicht gefunden oder Zugriff verweigert"));

        sparkontoRepository.delete(sparkonto);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal berechneGesamtsaldo(Long userId) {
        List<Sparkonto> konten = sparkontoRepository.findAllByBenutzerId(userId);

        BigDecimal gesamtsaldo = BigDecimal.ZERO;
        for (Sparkonto konto : konten) {
            BigDecimal kontoSaldo = buchungRepository.berechneSaldo(konto.getId());
            gesamtsaldo = gesamtsaldo.add(kontoSaldo);
        }

        return gesamtsaldo;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal berechneMonatlicherGesamtbedarf(Long userId) {
        List<Sparkonto> konten = sparkontoRepository.findAllByBenutzerId(userId);

        return konten.stream()
                .map(Sparkonto::getStandardbetrag)
                .filter(betrag -> betrag.compareTo(BigDecimal.ZERO) > 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
