package de.haushaltsbuch.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sparkonto",
       uniqueConstraints = @UniqueConstraint(columns = {"name", "benutzer_id"}))
public class Sparkonto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal standardbetrag;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "benutzer_id", nullable = false)
    private Benutzer benutzer;

    @Column(name = "erstellt_am", nullable = false)
    private LocalDateTime erstelltAm;

    public Sparkonto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getStandardbetrag() {
        return standardbetrag;
    }

    public void setStandardbetrag(BigDecimal standardbetrag) {
        this.standardbetrag = standardbetrag;
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
}
