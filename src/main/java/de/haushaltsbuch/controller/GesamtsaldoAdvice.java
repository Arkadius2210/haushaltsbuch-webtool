package de.haushaltsbuch.controller;

import de.haushaltsbuch.dto.BenutzerDetails;
import de.haushaltsbuch.service.SparkontoService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.math.BigDecimal;

@ControllerAdvice
public class GesamtsaldoAdvice {

    private final SparkontoService sparkontoService;

    public GesamtsaldoAdvice(SparkontoService sparkontoService) {
        this.sparkontoService = sparkontoService;
    }

    @ModelAttribute("saldo")
    public BigDecimal gesamtsaldo(@AuthenticationPrincipal BenutzerDetails user) {
        if (user == null) {
            return BigDecimal.ZERO;
        }
        return sparkontoService.berechneGesamtsaldo(user.getUserId());
    }
}
