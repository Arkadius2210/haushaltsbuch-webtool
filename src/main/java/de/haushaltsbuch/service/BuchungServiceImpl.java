package de.haushaltsbuch.service;

import de.haushaltsbuch.dto.BuchungDTO;
import de.haushaltsbuch.dto.BuchungErgebnisDTO;
import de.haushaltsbuch.dto.BuchungshistorieDTO;
import de.haushaltsbuch.dto.ManuelleBuchungFormDTO;
import de.haushaltsbuch.dto.MonatsbuchungFormDTO;
import de.haushaltsbuch.dto.MonatsbuchungVorschauDTO;
import de.haushaltsbuch.dto.UmbuchungFormDTO;
import de.haushaltsbuch.dto.VorschauPostenDTO;
import de.haushaltsbuch.exception.GleichesKontoException;
import de.haushaltsbuch.exception.ZugriffVerweigertException;
import de.haushaltsbuch.model.Buchung;
import de.haushaltsbuch.model.BuchungTyp;
import de.haushaltsbuch.model.Sparkonto;
import de.haushaltsbuch.repository.BuchungRepository;
import de.haushaltsbuch.repository.SparkontoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional
public class BuchungServiceImpl implements BuchungService {

    private final BuchungRepository buchungRepository;
    private final SparkontoRepository sparkontoRepository;

    public BuchungServiceImpl(BuchungRepository buchungRepository,
                              SparkontoRepository sparkontoRepository) {
        this.buchungRepository = buchungRepository;
        this.sparkontoRepository = sparkontoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public MonatsbuchungVorschauDTO vorschauErstellen(Long userId) {
        List<Sparkonto> sparkonten = sparkontoRepository.findAllByBenutzerId(userId);

        List<VorschauPostenDTO> posten = sparkonten.stream()
                .map(konto -> new VorschauPostenDTO(
                        konto.getId(),
                        konto.getName(),
                        konto.getStandardbetrag(),
                        true
                ))
                .toList();

        BigDecimal gesamtbetrag = posten.stream()
                .map(VorschauPostenDTO::betrag)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new MonatsbuchungVorschauDTO(posten, gesamtbetrag);
    }

    @Override
    public BuchungErgebnisDTO monatsbuchungDurchfuehren(MonatsbuchungFormDTO form, Long userId) {
        BigDecimal gesamtsaldoVorher = berechneGesamtsaldo(userId);

        List<Buchung> buchungen = new ArrayList<>();

        for (VorschauPostenDTO posten : form.posten()) {
            if (!posten.aktiv()) {
                continue;
            }

            Sparkonto konto = sparkontoRepository
                    .findByIdAndBenutzerId(posten.sparkontoId(), userId)
                    .orElseThrow(() -> new ZugriffVerweigertException(
                            "Sparkonto nicht gefunden oder Zugriff verweigert"));

            Buchung buchung = new Buchung();
            buchung.setSparkonto(konto);
            buchung.setDatum(form.datum());
            buchung.setBetrag(posten.betrag());
            buchung.setTyp(BuchungTyp.MONATSBUCHUNG);
            buchung.setBenutzer(konto.getBenutzer());
            buchung.setErstelltAm(LocalDateTime.now());

            buchungen.add(buchung);
        }

        buchungRepository.saveAll(buchungen);

        BigDecimal gesamtsaldoNachher = berechneGesamtsaldo(userId);
        BigDecimal saldoDifferenz = gesamtsaldoNachher.subtract(gesamtsaldoVorher);

        return new BuchungErgebnisDTO(
                gesamtsaldoVorher,
                gesamtsaldoNachher,
                saldoDifferenz,
                buchungen.stream().map(this::toBuchungDTO).toList()
        );
    }

    @Override
    public BuchungErgebnisDTO manuelleBuchungSpeichern(ManuelleBuchungFormDTO form, Long userId) {
        Sparkonto konto = sparkontoRepository
                .findByIdAndBenutzerId(form.sparkontoId(), userId)
                .orElseThrow(() -> new ZugriffVerweigertException(
                        "Sparkonto nicht gefunden oder Zugriff verweigert"));

        BigDecimal gesamtsaldoVorher = berechneGesamtsaldo(userId);

        Buchung buchung = new Buchung();
        buchung.setSparkonto(konto);
        buchung.setDatum(form.datum());
        buchung.setBetrag(form.betrag());
        buchung.setTyp(BuchungTyp.MANUELL);
        buchung.setBeschreibung(form.beschreibung());
        buchung.setBenutzer(konto.getBenutzer());
        buchung.setErstelltAm(LocalDateTime.now());

        buchungRepository.save(buchung);

        BigDecimal gesamtsaldoNachher = berechneGesamtsaldo(userId);
        BigDecimal saldoDifferenz = gesamtsaldoNachher.subtract(gesamtsaldoVorher);

        return new BuchungErgebnisDTO(
                gesamtsaldoVorher,
                gesamtsaldoNachher,
                saldoDifferenz,
                List.of(toBuchungDTO(buchung))
        );
    }

    @Override
    public BuchungErgebnisDTO umbuchungSpeichern(UmbuchungFormDTO form, Long userId) {
        if (form.quellkontoId().equals(form.zielkontoId())) {
            throw new GleichesKontoException("Quell- und Zielkonto dürfen nicht identisch sein");
        }

        Sparkonto quellkonto = sparkontoRepository
                .findByIdAndBenutzerId(form.quellkontoId(), userId)
                .orElseThrow(() -> new ZugriffVerweigertException(
                        "Quellkonto nicht gefunden oder Zugriff verweigert"));

        Sparkonto zielkonto = sparkontoRepository
                .findByIdAndBenutzerId(form.zielkontoId(), userId)
                .orElseThrow(() -> new ZugriffVerweigertException(
                        "Zielkonto nicht gefunden oder Zugriff verweigert"));

        BigDecimal gesamtsaldoVorher = berechneGesamtsaldo(userId);

        // Abbuchung vom Quellkonto
        Buchung abbuchung = new Buchung();
        abbuchung.setSparkonto(quellkonto);
        abbuchung.setDatum(form.datum());
        abbuchung.setBetrag(form.betrag().negate());
        abbuchung.setTyp(BuchungTyp.UMBUCHUNG);
        abbuchung.setBeschreibung("Umbuchung an " + zielkonto.getName());
        abbuchung.setBenutzer(quellkonto.getBenutzer());
        abbuchung.setErstelltAm(LocalDateTime.now());

        // Gutschrift auf Zielkonto
        Buchung gutschrift = new Buchung();
        gutschrift.setSparkonto(zielkonto);
        gutschrift.setDatum(form.datum());
        gutschrift.setBetrag(form.betrag());
        gutschrift.setTyp(BuchungTyp.UMBUCHUNG);
        gutschrift.setBeschreibung("Umbuchung von " + quellkonto.getName());
        gutschrift.setBenutzer(zielkonto.getBenutzer());
        gutschrift.setErstelltAm(LocalDateTime.now());

        buchungRepository.save(abbuchung);
        buchungRepository.save(gutschrift);

        // Gegenbuchung-Referenzen setzen
        abbuchung.setGegenbuchungId(gutschrift.getId());
        gutschrift.setGegenbuchungId(abbuchung.getId());
        buchungRepository.save(abbuchung);
        buchungRepository.save(gutschrift);

        BigDecimal gesamtsaldoNachher = berechneGesamtsaldo(userId);

        return new BuchungErgebnisDTO(
                gesamtsaldoVorher,
                gesamtsaldoNachher,
                BigDecimal.ZERO,
                List.of(toBuchungDTO(abbuchung), toBuchungDTO(gutschrift))
        );
    }

    @Override
    @Transactional(readOnly = true)
    public BuchungshistorieDTO getHistorie(Long sparkontoId, LocalDate von, LocalDate bis, Long userId) {
        Sparkonto sparkonto = sparkontoRepository.findByIdAndBenutzerId(sparkontoId, userId)
                .orElseThrow(() -> new ZugriffVerweigertException(
                        "Sparkonto nicht gefunden oder Zugriff verweigert"));

        List<Buchung> buchungen;
        if (von != null && bis != null) {
            buchungen = buchungRepository.findBySparkontoIdAndBenutzerIdAndDatumBetween(
                    sparkontoId, userId, von, bis);
        } else {
            buchungen = buchungRepository.findBySparkontoIdAndBenutzerId(sparkontoId, userId);
        }

        // Sort chronologically (oldest first) for running balance calculation
        buchungen = new ArrayList<>(buchungen);
        buchungen.sort(Comparator.comparing(Buchung::getDatum).thenComparing(Buchung::getErstelltAm));

        // Calculate running balance for each buchung
        BigDecimal runningBalance = BigDecimal.ZERO;
        List<BuchungDTO> buchungDTOs = new ArrayList<>();
        for (Buchung buchung : buchungen) {
            runningBalance = runningBalance.add(buchung.getBetrag());
            buchungDTOs.add(toBuchungDTOWithBalance(buchung, runningBalance));
        }

        // Reverse for display (newest first, as per requirement 5.1)
        List<BuchungDTO> reversed = new ArrayList<>(buchungDTOs);
        java.util.Collections.reverse(reversed);

        BigDecimal aktuellerSaldo = buchungRepository.berechneSaldo(sparkontoId);

        return new BuchungshistorieDTO(
                sparkonto.getId(),
                sparkonto.getName(),
                reversed,
                aktuellerSaldo
        );
    }

    private BigDecimal berechneGesamtsaldo(Long userId) {
        List<Sparkonto> konten = sparkontoRepository.findAllByBenutzerId(userId);

        BigDecimal gesamtsaldo = BigDecimal.ZERO;
        for (Sparkonto konto : konten) {
            BigDecimal kontoSaldo = buchungRepository.berechneSaldo(konto.getId());
            gesamtsaldo = gesamtsaldo.add(kontoSaldo);
        }

        return gesamtsaldo;
    }

    private BuchungDTO toBuchungDTO(Buchung buchung) {
        return new BuchungDTO(
                buchung.getId(),
                buchung.getSparkonto().getId(),
                buchung.getSparkonto().getName(),
                buchung.getDatum(),
                buchung.getBetrag(),
                buchung.getTyp().name(),
                buchung.getBeschreibung(),
                buchung.getErstelltAm(),
                null
        );
    }

    private BuchungDTO toBuchungDTOWithBalance(Buchung buchung, BigDecimal laufenderKontostand) {
        return new BuchungDTO(
                buchung.getId(),
                buchung.getSparkonto().getId(),
                buchung.getSparkonto().getName(),
                buchung.getDatum(),
                buchung.getBetrag(),
                buchung.getTyp().name(),
                buchung.getBeschreibung(),
                buchung.getErstelltAm(),
                laufenderKontostand
        );
    }
}
