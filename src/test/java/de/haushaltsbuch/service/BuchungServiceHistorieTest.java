package de.haushaltsbuch.service;

import de.haushaltsbuch.dto.BuchungshistorieDTO;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BuchungServiceHistorieTest {

    @Mock
    private BuchungRepository buchungRepository;

    @Mock
    private SparkontoRepository sparkontoRepository;

    @InjectMocks
    private BuchungServiceImpl buchungService;

    private Benutzer benutzer;
    private Sparkonto sparkonto;

    @BeforeEach
    void setUp() {
        benutzer = new Benutzer();
        benutzer.setId(1L);
        benutzer.setUsername("testuser");

        sparkonto = new Sparkonto();
        sparkonto.setId(10L);
        sparkonto.setName("Auto-Rücklage");
        sparkonto.setStandardbetrag(new BigDecimal("250.00"));
        sparkonto.setBenutzer(benutzer);
        sparkonto.setErstelltAm(LocalDateTime.now());
    }

    @Test
    void getHistorie_alleBuchungen_gibtChronologischSortiertZurueck() {
        Long userId = 1L;
        Long sparkontoId = 10L;

        Buchung buchung1 = createBuchung(1L, LocalDate.of(2024, 1, 15), new BigDecimal("250.00"),
                BuchungTyp.MONATSBUCHUNG, "Januar");
        Buchung buchung2 = createBuchung(2L, LocalDate.of(2024, 2, 15), new BigDecimal("250.00"),
                BuchungTyp.MONATSBUCHUNG, "Februar");
        Buchung buchung3 = createBuchung(3L, LocalDate.of(2024, 3, 10), new BigDecimal("-500.00"),
                BuchungTyp.MANUELL, "Werkstatt");

        when(sparkontoRepository.findByIdAndBenutzerId(sparkontoId, userId))
                .thenReturn(Optional.of(sparkonto));
        when(buchungRepository.findBySparkontoIdAndBenutzerId(sparkontoId, userId))
                .thenReturn(List.of(buchung2, buchung1, buchung3)); // unsorted
        when(buchungRepository.berechneSaldo(sparkontoId))
                .thenReturn(BigDecimal.ZERO);

        BuchungshistorieDTO historie = buchungService.getHistorie(sparkontoId, null, null, userId);

        // Newest first for display
        assertThat(historie.buchungen()).hasSize(3);
        assertThat(historie.buchungen().get(0).datum()).isEqualTo(LocalDate.of(2024, 3, 10));
        assertThat(historie.buchungen().get(1).datum()).isEqualTo(LocalDate.of(2024, 2, 15));
        assertThat(historie.buchungen().get(2).datum()).isEqualTo(LocalDate.of(2024, 1, 15));
    }

    @Test
    void getHistorie_berechnetLaufendenKontostand() {
        Long userId = 1L;
        Long sparkontoId = 10L;

        Buchung buchung1 = createBuchung(1L, LocalDate.of(2024, 1, 15), new BigDecimal("250.00"),
                BuchungTyp.MONATSBUCHUNG, "Januar");
        Buchung buchung2 = createBuchung(2L, LocalDate.of(2024, 2, 15), new BigDecimal("250.00"),
                BuchungTyp.MONATSBUCHUNG, "Februar");
        Buchung buchung3 = createBuchung(3L, LocalDate.of(2024, 3, 10), new BigDecimal("-500.00"),
                BuchungTyp.MANUELL, "Werkstatt");

        when(sparkontoRepository.findByIdAndBenutzerId(sparkontoId, userId))
                .thenReturn(Optional.of(sparkonto));
        when(buchungRepository.findBySparkontoIdAndBenutzerId(sparkontoId, userId))
                .thenReturn(List.of(buchung1, buchung2, buchung3));
        when(buchungRepository.berechneSaldo(sparkontoId))
                .thenReturn(BigDecimal.ZERO);

        BuchungshistorieDTO historie = buchungService.getHistorie(sparkontoId, null, null, userId);

        // Running balance: 250, 500, 0 (chronological order)
        // Reversed for display: newest first
        assertThat(historie.buchungen().get(0).laufenderKontostand()).isEqualByComparingTo("0");    // buchung3
        assertThat(historie.buchungen().get(1).laufenderKontostand()).isEqualByComparingTo("500");  // buchung2
        assertThat(historie.buchungen().get(2).laufenderKontostand()).isEqualByComparingTo("250");  // buchung1
    }

    @Test
    void getHistorie_mitZeitraumfilter_nutztDatumBetween() {
        Long userId = 1L;
        Long sparkontoId = 10L;
        LocalDate von = LocalDate.of(2024, 2, 1);
        LocalDate bis = LocalDate.of(2024, 2, 28);

        Buchung buchung = createBuchung(2L, LocalDate.of(2024, 2, 15), new BigDecimal("250.00"),
                BuchungTyp.MONATSBUCHUNG, "Februar");

        when(sparkontoRepository.findByIdAndBenutzerId(sparkontoId, userId))
                .thenReturn(Optional.of(sparkonto));
        when(buchungRepository.findBySparkontoIdAndBenutzerIdAndDatumBetween(sparkontoId, userId, von, bis))
                .thenReturn(List.of(buchung));
        when(buchungRepository.berechneSaldo(sparkontoId))
                .thenReturn(new BigDecimal("500.00"));

        BuchungshistorieDTO historie = buchungService.getHistorie(sparkontoId, von, bis, userId);

        assertThat(historie.buchungen()).hasSize(1);
        assertThat(historie.buchungen().get(0).betrag()).isEqualByComparingTo("250.00");
        assertThat(historie.buchungen().get(0).laufenderKontostand()).isEqualByComparingTo("250.00");
    }

    @Test
    void getHistorie_fremdesKonto_wirftZugriffVerweigert() {
        Long userId = 1L;
        Long sparkontoId = 99L;

        when(sparkontoRepository.findByIdAndBenutzerId(sparkontoId, userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> buchungService.getHistorie(sparkontoId, null, null, userId))
                .isInstanceOf(ZugriffVerweigertException.class);
    }

    @Test
    void getHistorie_leereHistorie_gibtLeereListeZurueck() {
        Long userId = 1L;
        Long sparkontoId = 10L;

        when(sparkontoRepository.findByIdAndBenutzerId(sparkontoId, userId))
                .thenReturn(Optional.of(sparkonto));
        when(buchungRepository.findBySparkontoIdAndBenutzerId(sparkontoId, userId))
                .thenReturn(List.of());
        when(buchungRepository.berechneSaldo(sparkontoId))
                .thenReturn(BigDecimal.ZERO);

        BuchungshistorieDTO historie = buchungService.getHistorie(sparkontoId, null, null, userId);

        assertThat(historie.buchungen()).isEmpty();
        assertThat(historie.aktuellerSaldo()).isEqualByComparingTo("0");
        assertThat(historie.sparkontoName()).isEqualTo("Auto-Rücklage");
        assertThat(historie.sparkontoId()).isEqualTo(10L);
    }

    @Test
    void getHistorie_enthaltSparkontoInfoUndAktuellenSaldo() {
        Long userId = 1L;
        Long sparkontoId = 10L;

        Buchung buchung = createBuchung(1L, LocalDate.of(2024, 1, 15), new BigDecimal("250.00"),
                BuchungTyp.MONATSBUCHUNG, "Januar");

        when(sparkontoRepository.findByIdAndBenutzerId(sparkontoId, userId))
                .thenReturn(Optional.of(sparkonto));
        when(buchungRepository.findBySparkontoIdAndBenutzerId(sparkontoId, userId))
                .thenReturn(List.of(buchung));
        when(buchungRepository.berechneSaldo(sparkontoId))
                .thenReturn(new BigDecimal("1500.00"));

        BuchungshistorieDTO historie = buchungService.getHistorie(sparkontoId, null, null, userId);

        assertThat(historie.sparkontoId()).isEqualTo(10L);
        assertThat(historie.sparkontoName()).isEqualTo("Auto-Rücklage");
        assertThat(historie.aktuellerSaldo()).isEqualByComparingTo("1500.00");
    }

    @Test
    void getHistorie_buchungDTOEnthaeltAlleFelder() {
        Long userId = 1L;
        Long sparkontoId = 10L;

        Buchung buchung = createBuchung(42L, LocalDate.of(2024, 5, 20), new BigDecimal("-100.00"),
                BuchungTyp.MANUELL, "Tanken");

        when(sparkontoRepository.findByIdAndBenutzerId(sparkontoId, userId))
                .thenReturn(Optional.of(sparkonto));
        when(buchungRepository.findBySparkontoIdAndBenutzerId(sparkontoId, userId))
                .thenReturn(List.of(buchung));
        when(buchungRepository.berechneSaldo(sparkontoId))
                .thenReturn(new BigDecimal("-100.00"));

        BuchungshistorieDTO historie = buchungService.getHistorie(sparkontoId, null, null, userId);

        var dto = historie.buchungen().get(0);
        assertThat(dto.id()).isEqualTo(42L);
        assertThat(dto.sparkontoId()).isEqualTo(10L);
        assertThat(dto.sparkontoName()).isEqualTo("Auto-Rücklage");
        assertThat(dto.datum()).isEqualTo(LocalDate.of(2024, 5, 20));
        assertThat(dto.betrag()).isEqualByComparingTo("-100.00");
        assertThat(dto.typ()).isEqualTo("MANUELL");
        assertThat(dto.beschreibung()).isEqualTo("Tanken");
        assertThat(dto.laufenderKontostand()).isEqualByComparingTo("-100.00");
    }

    private Buchung createBuchung(Long id, LocalDate datum, BigDecimal betrag,
                                   BuchungTyp typ, String beschreibung) {
        Buchung buchung = new Buchung();
        buchung.setId(id);
        buchung.setSparkonto(sparkonto);
        buchung.setDatum(datum);
        buchung.setBetrag(betrag);
        buchung.setTyp(typ);
        buchung.setBeschreibung(beschreibung);
        buchung.setBenutzer(benutzer);
        buchung.setErstelltAm(LocalDateTime.of(datum.getYear(), datum.getMonthValue(),
                datum.getDayOfMonth(), 10, 0));
        return buchung;
    }
}
