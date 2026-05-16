package de.haushaltsbuch.config;

import de.haushaltsbuch.dto.BenutzerDetails;
import de.haushaltsbuch.model.Benutzer;
import de.haushaltsbuch.repository.BenutzerRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class BenutzerDetailsService implements UserDetailsService {

    private final BenutzerRepository benutzerRepository;

    public BenutzerDetailsService(BenutzerRepository benutzerRepository) {
        this.benutzerRepository = benutzerRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Benutzer benutzer = benutzerRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Benutzer nicht gefunden: " + username));

        return new BenutzerDetails(
                benutzer.getId(),
                benutzer.getUsername(),
                benutzer.getPasswordHash()
        );
    }
}
