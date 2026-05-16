package de.haushaltsbuch.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "buchung")
public class Buchung {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sparkonto_id", nullable = false)
    private Sparkonto sparkonto;

    @Column(nullable = false)
    private LocalDate datum;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal betrag;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BuchungTyp typ;

    @Column(length = 255)
    private String beschreibung;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "benutzer_id", nullable = false)
    private Benutzer benutzer;

    @Column(name = "erstellt_am", nullable = false)
    private LocalDateTime erstelltAm;

    @Column(name = "gegenbuchung_id")
    private Long gegenbuchungId;

    public Buchung() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Sparkonto getSparkonto() {
        return sparkonto;
    }

    public void setSparkonto(Sparkonto sparkonto) {
        this.sparkonto = sparkonto;
    }

    public LocalDate getDatum() {
        return datum;
    }

    public void setDatum(LocalDate datum) {
        this.datum = datum;
    }

    public BigDecimal getBetrag() {
        return betrag;
    }

    public void setBetrag(BigDecimal betrag) {
        this.betrag = betrag;
    }

    public BuchungTyp getTyp() {
        return typ;
    }

    public void setTyp(BuchungTyp typ) {
        this.typ = typ;
    }

    public String getBeschreibung() {
        return beschreibung;
    }

    public void setBeschreibung(String beschreibung) {
        this.beschreibung = beschreibung;
    }

    public Benutzer getBenutzer() {
        return benutzer;
    }

    public void setBenutzer(Benutzer benutzer) {
        this.benutzer = benutzer;
    }

    public LocalDateTime getErstelltAm() {
        return erstelltAm;
    }

    public void setErstelltAm(LocalDateTime erstelltAm) {
        this.erstelltAm = erstelltAm;
    }

    public Long getGegenbuchungId() {
        return gegenbuchungId;
    }

    public void setGegenbuchungId(Long gegenbuchungId) {
        this.gegenbuchungId = gegenbuchungId;
    }
}
