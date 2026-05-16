package de.haushaltsbuch.repository;

import de.haushaltsbuch.model.Jahresausgabe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JahresausgabeRepository extends JpaRepository<Jahresausgabe, Long> {

    List<Jahresausgabe> findAllByBenutzerId(Long benutzerId);

    Optional<Jahresausgabe> findByIdAndBenutzerId(Long id, Long benutzerId);
}
