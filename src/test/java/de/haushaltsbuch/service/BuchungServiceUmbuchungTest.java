package de.haushaltsbuch.service;

import de.haushaltsbuch.dto.BuchungErgebnisDTO;
import de.haushaltsbuch.dto.UmbuchungFormDTO;
import de.haushaltsbuch.exception.GleichesKontoException;
import de.haushaltsbuch.exception.ZugriffVerweigertException;
import de.haushaltsbuch.model.Benutzer;
import de.haushaltsbuch.model.Buchung;
import de.haushaltsbuch.model.BuchungTyp;
import de.haushaltsbuch.model.Sparkonto;
import de.haushaltsbuch.repository.BuchungRepository;
import de.haushaltsbuch.repository.SparkontoRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BuchungServiceUmbuchungTest {

    @Mock
    private BuchungRepository buchungRepository;

    @Mock
    private SparkontoRepository sparkontoRepository;

    @InjectMocks
    private BuchungServiceImpl buchungService;

    private Benutzer benutzer;
    private Sparkonto quellkonto;
    private Sparkonto zielkonto;

    @BeforeEach
    void setUp() {
        benutzer = new Benutzer();
        benutzer.setId(1L);
        benutzer.setUsername("testuser");
        benutzer.setErstelltAm(LocalDateTime.now());

        quellkonto = new Sparkonto();
        quellkonto.setId(10L);
        quellkonto.setName("Urlaub");
        quellkonto.setStandardbetrag(new BigDecimal("200.00"));
        quellkonto.setBenutzer(benutzer);
        quellkonto.setErstelltAm(LocalDateTime.now());

        zielkonto = new Sparkonto();
        zielkonto.setId(20L);
        zielkonto.setName("Auto");
        zielkonto.setStandardbetrag(new BigDecimal("250.00"));
        zielkonto.setBenutzer(benutzer);
        zielkonto.setErstelltAm(LocalDateTime.now());
    }

    @Test
    void umbuchungSpeichern_erfolgreicheUmbuchung() {
        UmbuchungFormDTO form = new UmbuchungFormDTO(10L, 20L,
                new BigDecimal("500.00"), LocalDate.of(2024, 3, 15), "Test-Umbuchung");

        when(sparkontoRepository.findByIdAndBenutzerId(10L, 1L))
                .thenReturn(Optional.of(quellkonto));
        when(sparkontoRepository.findByIdAndBenutzerId(20L, 1L))
                .thenReturn(Optional.of(zielkonto));
        when(sparkontoRepository.findAllByBenutzerId(1L))
                .thenReturn(List.of(quellkonto, zielkonto));
        when(buchungRepository.berechneSaldo(10L))
                .thenReturn(new BigDecimal("1000.00"));
        when(buchungRepository.berechneSaldo(20L))
                .thenReturn(new BigDecimal("500.00"));

        // Mock save to assign IDs
        when(buchungRepository.save(any(Buchung.class))).thenAnswer(invocation -> {
            Buchung b = invocation.getArgument(0);
            if (b.getId() == null) {
                if (b.getBetrag().compareTo(BigDecimal.ZERO) < 0) {
                    b.setId(100L);
                } else {
                    b.setId(101L);
                }
            }
            return b;
        });

        BuchungErgebnisDTO ergebnis = buchungService.umbuchungSpeichern(form, 1L);

        assertThat(ergebnis.saldoDifferenz()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(ergebnis.gebuchtePosten()).hasSize(2);

        // Verify 4 saves: initial save of both + update with gegenbuchungId
        verify(buchungRepository, times(4)).save(any(Buchung.class));
    }

    @Test
    void umbuchungSpeichern_setztGegenbuchungReferenzen() {
        UmbuchungFormDTO form = new UmbuchungFormDTO(10L, 20L,
                new BigDecimal("300.00"), LocalDate.of(2024, 6, 1), null);

        when(sparkontoRepository.findByIdAndBenutzerId(10L, 1L))
                .thenReturn(Optional.of(quellkonto));
        when(sparkontoRepository.findByIdAndBenutzerId(20L, 1L))
                .thenReturn(Optional.of(zielkonto));
        when(sparkontoRepository.findAllByBenutzerId(1L))
                .thenReturn(List.of(quellkonto, zielkonto));
        when(buchungRepository.berechneSaldo(10L))
                .thenReturn(BigDecimal.ZERO);
        when(buchungRepository.berechneSaldo(20L))
                .thenReturn(BigDecimal.ZERO);

        when(buchungRepository.save(any(Buchung.class))).thenAnswer(invocation -> {
            Buchung b = invocation.getArgument(0);
            if (b.getId() == null) {
                if (b.getBetrag().compareTo(BigDecimal.ZERO) < 0) {
                    b.setId(200L);
                } else {
                    b.setId(201L);
                }
            }
            return b;
        });

        buchungService.umbuchungSpeichern(form, 1L);

        ArgumentCaptor<Buchung> captor = ArgumentCaptor.forClass(Buchung.class);
        verify(buchungRepository, times(4)).save(captor.capture());

        List<Buchung> savedBuchungen = captor.getAllValues();
        // Last two saves should have gegenbuchungId set
        Buchung abbuchungFinal = savedBuchungen.get(2);
        Buchung gutschriftFinal = savedBuchungen.get(3);

        assertThat(abbuchungFinal.getGegenbuchungId()).isEqualTo(201L);
        assertThat(gutschriftFinal.getGegenbuchungId()).isEqualTo(200L);
    }

    @Test
    void umbuchungSpeichern_erstelltAbbuchungUndGutschrift() {
        UmbuchungFormDTO form = new UmbuchungFormDTO(10L, 20L,
                new BigDecimal("150.00"), LocalDate.of(2024, 4, 1), "Umverteilung");

        when(sparkontoRepository.findByIdAndBenutzerId(10L, 1L))
                .thenReturn(Optional.of(quellkonto));
        when(sparkontoRepository.findByIdAndBenutzerId(20L, 1L))
                .thenReturn(Optional.of(zielkonto));
        when(sparkontoRepository.findAllByBenutzerId(1L))
                .thenReturn(List.of(quellkonto, zielkonto));
        when(buchungRepository.berechneSaldo(10L))
                .thenReturn(BigDecimal.ZERO);
        when(buchungRepository.berechneSaldo(20L))
                .thenReturn(BigDecimal.ZERO);

        when(buchungRepository.save(any(Buchung.class))).thenAnswer(invocation -> {
            Buchung b = invocation.getArgument(0);
            if (b.getId() == null) {
                if (b.getBetrag().compareTo(BigDecimal.ZERO) < 0) {
                    b.setId(300L);
                } else {
                    b.setId(301L);
                }
            }
            return b;
        });

        BuchungErgebnisDTO ergebnis = buchungService.umbuchungSpeichern(form, 1L);

        // Abbuchung: negative amount on Quellkonto
        assertThat(ergebnis.gebuchtePosten().get(0).betrag())
                .isEqualByComparingTo(new BigDecimal("-150.00"));
        assertThat(ergebnis.gebuchtePosten().get(0).sparkontoName()).isEqualTo("Urlaub");
        assertThat(ergebnis.gebuchtePosten().get(0).typ()).isEqualTo("UMBUCHUNG");

        // Gutschrift: positive amount on Zielkonto
        assertThat(ergebnis.gebuchtePosten().get(1).betrag())
                .isEqualByComparingTo(new BigDecimal("150.00"));
        assertThat(ergebnis.gebuchtePosten().get(1).sparkontoName()).isEqualTo("Auto");
        assertThat(ergebnis.gebuchtePosten().get(1).typ()).isEqualTo("UMBUCHUNG");
    }

    @Test
    void umbuchungSpeichern_beschreibungEnthältKontonamen() {
        UmbuchungFormDTO form = new UmbuchungFormDTO(10L, 20L,
                new BigDecimal("100.00"), LocalDate.of(2024, 5, 1), null);

        when(sparkontoRepository.findByIdAndBenutzerId(10L, 1L))
                .thenReturn(Optional.of(quellkonto));
        when(sparkontoRepository.findByIdAndBenutzerId(20L, 1L))
                .thenReturn(Optional.of(zielkonto));
        when(sparkontoRepository.findAllByBenutzerId(1L))
                .thenReturn(List.of(quellkonto, zielkonto));
        when(buchungRepository.berechneSaldo(10L))
                .thenReturn(BigDecimal.ZERO);
        when(buchungRepository.berechneSaldo(20L))
                .thenReturn(BigDecimal.ZERO);

        when(buchungRepository.save(any(Buchung.class))).thenAnswer(invocation -> {
            Buchung b = invocation.getArgument(0);
            if (b.getId() == null) {
                b.setId(b.getBetrag().compareTo(BigDecimal.ZERO) < 0 ? 400L : 401L);
            }
            return b;
        });

        ArgumentCaptor<Buchung> captor = ArgumentCaptor.forClass(Buchung.class);

        buchungService.umbuchungSpeichern(form, 1L);

        verify(buchungRepository, times(4)).save(captor.capture());
        List<Buchung> saved = captor.getAllValues();

        // First save: Abbuchung
        assertThat(saved.get(0).getBeschreibung()).isEqualTo("Umbuchung an Auto");
        // Second save: Gutschrift
        assertThat(saved.get(1).getBeschreibung()).isEqualTo("Umbuchung von Urlaub");
    }

    @Test
    void umbuchungSpeichern_gleichesKontoWirftException() {
        UmbuchungFormDTO form = new UmbuchungFormDTO(10L, 10L,
                new BigDecimal("100.00"), LocalDate.of(2024, 3, 1), null);

        assertThatThrownBy(() -> buchungService.umbuchungSpeichern(form, 1L))
                .isInstanceOf(GleichesKontoException.class)
                .hasMessageContaining("Quell- und Zielkonto dürfen nicht identisch sein");
    }

    @Test
    void umbuchungSpeichern_quellkontoNichtGefundenWirftException() {
        UmbuchungFormDTO form = new UmbuchungFormDTO(99L, 20L,
                new BigDecimal("100.00"), LocalDate.of(2024, 3, 1), null);

        when(sparkontoRepository.findByIdAndBenutzerId(99L, 1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> buchungService.umbuchungSpeichern(form, 1L))
                .isInstanceOf(ZugriffVerweigertException.class);
    }

    @Test
    void umbuchungSpeichern_zielkontoNichtGefundenWirftException() {
        UmbuchungFormDTO form = new UmbuchungFormDTO(10L, 99L,
                new BigDecimal("100.00"), LocalDate.of(2024, 3, 1), null);

        when(sparkontoRepository.findByIdAndBenutzerId(10L, 1L))
                .thenReturn(Optional.of(quellkonto));
        when(sparkontoRepository.findByIdAndBenutzerId(99L, 1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> buchungService.umbuchungSpeichern(form, 1L))
                .isInstanceOf(ZugriffVerweigertException.class);
    }

    @Test
    void umbuchungSpeichern_gesamtsaldoBleibtUnveraendert() {
        UmbuchungFormDTO form = new UmbuchungFormDTO(10L, 20L,
                new BigDecimal("750.00"), LocalDate.of(2024, 7, 1), null);

        when(sparkontoRepository.findByIdAndBenutzerId(10L, 1L))
                .thenReturn(Optional.of(quellkonto));
        when(sparkontoRepository.findByIdAndBenutzerId(20L, 1L))
                .thenReturn(Optional.of(zielkonto));
        when(sparkontoRepository.findAllByBenutzerId(1L))
                .thenReturn(List.of(quellkonto, zielkonto));
        when(buchungRepository.berechneSaldo(10L))
                .thenReturn(new BigDecimal("2000.00"));
        when(buchungRepository.berechneSaldo(20L))
                .thenReturn(new BigDecimal("3000.00"));

        when(buchungRepository.save(any(Buchung.class))).thenAnswer(invocation -> {
            Buchung b = invocation.getArgument(0);
            if (b.getId() == null) {
                b.setId(b.getBetrag().compareTo(BigDecimal.ZERO) < 0 ? 500L : 501L);
            }
            return b;
        });

        BuchungErgebnisDTO ergebnis = buchungService.umbuchungSpeichern(form, 1L);

        // Gesamtsaldo vorher und nachher should be the same (5000)
        assertThat(ergebnis.gesamtsaldoVorher()).isEqualByComparingTo(new BigDecimal("5000.00"));
        assertThat(ergebnis.gesamtsaldoNachher()).isEqualByComparingTo(new BigDecimal("5000.00"));
        assertThat(ergebnis.saldoDifferenz()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
