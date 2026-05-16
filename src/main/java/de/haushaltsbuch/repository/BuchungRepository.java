package de.haushaltsbuch.repository;

import de.haushaltsbuch.model.Buchung;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface BuchungRepository extends JpaRepository<Buchung, Long> {

    List<Buchung> findBySparkontoIdAndBenutzerId(Long sparkontoId, Long benutzerId);

    List<Buchung> findBySparkontoIdAndBenutzerIdAndDatumBetween(
            Long sparkontoId, Long benutzerId, LocalDate von, LocalDate bis);

    @Query("SELECT COALESCE(SUM(b.betrag), 0) FROM Buchung b WHERE b.sparkonto.id = :sparkontoId")
    BigDecimal berechneSaldo(@Param("sparkontoId") Long sparkontoId);
}
