package de.haushaltsbuch.repository;

import de.haushaltsbuch.model.Sparkonto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SparkontoRepository extends JpaRepository<Sparkonto, Long> {

    List<Sparkonto> findAllByBenutzerId(Long benutzerId);

    Optional<Sparkonto> findByIdAndBenutzerId(Long id, Long benutzerId);

    boolean existsByIdAndBenutzerId(Long id, Long benutzerId);
}
