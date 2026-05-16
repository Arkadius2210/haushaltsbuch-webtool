package de.haushaltsbuch.repository;

import de.haushaltsbuch.model.Benutzer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BenutzerRepository extends JpaRepository<Benutzer, Long> {

    Optional<Benutzer> findByUsername(String username);
}
