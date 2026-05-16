package de.haushaltsbuch.config;

import de.haushaltsbuch.model.Benutzer;
import de.haushaltsbuch.repository.BenutzerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DefaultUserInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DefaultUserInitializer.class);

    private final BenutzerRepository benutzerRepository;
    private final PasswordEncoder passwordEncoder;

    public DefaultUserInitializer(BenutzerRepository benutzerRepository, PasswordEncoder passwordEncoder) {
        this.benutzerRepository = benutzerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        benutzerRepository.findByUsername("admin").ifPresentOrElse(
                benutzer -> {
                    // Update password hash to ensure it matches
                    benutzer.setPasswordHash(passwordEncoder.encode("admin"));
                    benutzerRepository.save(benutzer);
                    log.info("Default user 'admin' password updated.");
                },
                () -> {
                    Benutzer benutzer = new Benutzer();
                    benutzer.setUsername("admin");
                    benutzer.setPasswordHash(passwordEncoder.encode("admin"));
                    benutzer.setErstelltAm(LocalDateTime.now());
                    benutzerRepository.save(benutzer);
                    log.info("Default user 'admin' created.");
                }
        );
    }
}
