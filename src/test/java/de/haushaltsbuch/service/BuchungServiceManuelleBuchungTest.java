package de.haushaltsbuch.service;

import de.haushaltsbuch.dto.BuchungErgebnisDTO;
import de.haushaltsbuch.dto.ManuelleBuchungFormDTO;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BuchungServiceManuelleBuchungTest {

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
    void manuelleBuchungSpeichern_zubuchung_erhohtSaldo() {
        Long userId = 1L;
        ManuelleBuchungFormDTO form = new ManuelleBuchungFormDTO(
                10L,
                new BigDecimal("500.00"),
                LocalDate.of(2024, 3, 15),
                "Bonus erhalten"
        );

        when(sparkontoRepository.findByIdAndBenutzerId(10L, userId))
                .thenReturn(Optional.of(sparkonto));
        when(sparkontoRepository.findAllByBenutzerId(userId))
                .thenReturn(List.of(sparkonto));
        when(buchungRepository.berechneSaldo(10L))
                .thenReturn(new BigDecimal("1000.00"))
                .thenReturn(new BigDecimal("1500.00"));
        when(buchungRepository.save(any(Buchung.class)))
                .thenAnswer(invocation -> {
                    Buchung b = invocation.getArgument(0);
                    b.setId(100L);
                    return b;
                });

        BuchungErgebnisDTO ergebnis = buchungService.manuelleBuchungSpeichern(form, userId);

        assertThat(ergebnis.gesamtsaldoVorher()).isEqualByComparingTo("1000.00");
        assertThat(ergebnis.gesamtsaldoNachher()).isEqualByComparingTo("1500.00");
        assertThat(ergebnis.saldoDifferenz()).isEqualByComparingTo("500.00");
        assertThat(ergebnis.gebuchtePosten()).hasSize(1);
        assertThat(ergebnis.gebuchtePosten().get(0).betrag()).isEqualByComparingTo("500.00");
        assertThat(ergebnis.gebuchtePosten().get(0).typ()).isEqualTo("MANUELL");
    }

    @Test
    void manuelleBuchungSpeichern_abbuchung_verringertSaldo() {
        Long userId = 1L;
        ManuelleBuchungFormDTO form = new ManuelleBuchungFormDTO(
                10L,
                new BigDecimal("-850.00"),
                LocalDate.of(2024, 3, 15),
                "Werkstatt-Rechnung"
        );

        when(sparkontoRepository.findByIdAndBenutzerId(10L, userId))
                .thenReturn(Optional.of(sparkonto));
        when(sparkontoRepository.findAllByBenutzerId(userId))
                .thenReturn(List.of(sparkonto));
        when(buchungRepository.berechneSaldo(10L))
                .thenReturn(new BigDecimal("2000.00"))
                .thenReturn(new BigDecimal("1150.00"));
        when(buchungRepository.save(any(Buchung.class)))
                .thenAnswer(invocation -> {
                    Buchung b = invocation.getArgument(0);
                    b.setId(101L);
                    return b;
                });

        BuchungErgebnisDTO ergebnis = buchungService.manuelleBuchungSpeichern(form, userId);

        assertThat(ergebnis.gesamtsaldoVorher()).isEqualByComparingTo("2000.00");
        assertThat(ergebnis.gesamtsaldoNachher()).isEqualByComparingTo("1150.00");
        assertThat(ergebnis.saldoDifferenz()).isEqualByComparingTo("-850.00");
        assertThat(ergebnis.gebuchtePosten()).hasSize(1);
        assertThat(ergebnis.gebuchtePosten().get(0).betrag()).isEqualByComparingTo("-850.00");
    }

    @Test
    void manuelleBuchungSpeichern_setzBuchungTypManuell() {
        Long userId = 1L;
        ManuelleBuchungFormDTO form = new ManuelleBuchungFormDTO(
                10L,
                new BigDecimal("100.00"),
                LocalDate.of(2024, 6, 1),
                null
        );

        when(sparkontoRepository.findByIdAndBenutzerId(10L, userId))
                .thenReturn(Optional.of(sparkonto));
        when(sparkontoRepository.findAllByBenutzerId(userId))
                .thenReturn(List.of(sparkonto));
        when(buchungRepository.berechneSaldo(10L))
                .thenReturn(BigDecimal.ZERO)
                .thenReturn(new BigDecimal("100.00"));
        when(buchungRepository.save(any(Buchung.class)))
                .thenAnswer(invocation -> {
                    Buchung b = invocation.getArgument(0);
                    b.setId(102L);
                    return b;
                });

        buchungService.manuelleBuchungSpeichern(form, userId);

        ArgumentCaptor<Buchung> captor = ArgumentCaptor.forClass(Buchung.class);
        verify(buchungRepository).save(captor.capture());

        Buchung gespeichert = captor.getValue();
        assertThat(gespeichert.getTyp()).isEqualTo(BuchungTyp.MANUELL);
        assertThat(gespeichert.getSparkonto()).isEqualTo(sparkonto);
        assertThat(gespeichert.getDatum()).isEqualTo(LocalDate.of(2024, 6, 1));
        assertThat(gespeichert.getBetrag()).isEqualByComparingTo("100.00");
        assertThat(gespeichert.getBeschreibung()).isNull();
        assertThat(gespeichert.getBenutzer()).isEqualTo(benutzer);
        assertThat(gespeichert.getErstelltAm()).isNotNull();
    }

    @Test
    void manuelleBuchungSpeichern_fremdesKonto_wirftZugriffVerweigert() {
        Long userId = 1L;
        ManuelleBuchungFormDTO form = new ManuelleBuchungFormDTO(
                99L,
                new BigDecimal("100.00"),
                LocalDate.of(2024, 3, 15),
                "Versuch"
        );

        when(sparkontoRepository.findByIdAndBenutzerId(99L, userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> buchungService.manuelleBuchungSpeichern(form, userId))
                .isInstanceOf(ZugriffVerweigertException.class);

        verify(buchungRepository, never()).save(any(Buchung.class));
    }

    @Test
    void manuelleBuchungSpeichern_mitBeschreibung_speichertBeschreibung() {
        Long userId = 1L;
        String beschreibung = "Werkstatt-Rechnung Inspektion";
        ManuelleBuchungFormDTO form = new ManuelleBuchungFormDTO(
                10L,
                new BigDecimal("-850.00"),
                LocalDate.of(2024, 3, 15),
                beschreibung
        );

        when(sparkontoRepository.findByIdAndBenutzerId(10L, userId))
                .thenReturn(Optional.of(sparkonto));
        when(sparkontoRepository.findAllByBenutzerId(userId))
                .thenReturn(List.of(sparkonto));
        when(buchungRepository.berechneSaldo(10L))
                .thenReturn(new BigDecimal("2000.00"))
                .thenReturn(new BigDecimal("1150.00"));
        when(buchungRepository.save(any(Buchung.class)))
                .thenAnswer(invocation -> {
                    Buchung b = invocation.getArgument(0);
                    b.setId(103L);
                    return b;
                });

        buchungService.manuelleBuchungSpeichern(form, userId);

        ArgumentCaptor<Buchung> captor = ArgumentCaptor.forClass(Buchung.class);
        verify(buchungRepository).save(captor.capture());

        assertThat(captor.getValue().getBeschreibung()).isEqualTo(beschreibung);
    }
}
