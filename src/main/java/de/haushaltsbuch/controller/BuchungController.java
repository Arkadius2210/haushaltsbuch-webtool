package de.haushaltsbuch.controller;

import de.haushaltsbuch.dto.BenutzerDetails;
import de.haushaltsbuch.dto.BuchungErgebnisDTO;
import de.haushaltsbuch.dto.BuchungshistorieDTO;
import de.haushaltsbuch.dto.ManuelleBuchungFormDTO;
import de.haushaltsbuch.dto.MonatsbuchungFormDTO;
import de.haushaltsbuch.dto.MonatsbuchungVorschauDTO;
import de.haushaltsbuch.dto.SparkontoDTO;
import de.haushaltsbuch.dto.UmbuchungFormDTO;
import de.haushaltsbuch.exception.GleichesKontoException;
import de.haushaltsbuch.service.BuchungService;
import de.haushaltsbuch.service.SparkontoService;

import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/buchungen")
public class BuchungController {

    private final BuchungService buchungService;
    private final SparkontoService sparkontoService;

    public BuchungController(BuchungService buchungService, SparkontoService sparkontoService) {
        this.buchungService = buchungService;
        this.sparkontoService = sparkontoService;
    }

    // --- Monatsbuchung ---

    @GetMapping("/monatsbuchung")
    public String monatsbuchungVorschau(@AuthenticationPrincipal BenutzerDetails user, Model model) {
        MonatsbuchungVorschauDTO vorschau = buchungService.vorschauErstellen(user.getUserId());
        model.addAttribute("vorschau", vorschau);
        return "buchungen/monatsbuchung";
    }

    @PostMapping("/monatsbuchung")
    public String monatsbuchungDurchfuehren(@Valid MonatsbuchungFormDTO form,
                                            BindingResult result,
                                            @AuthenticationPrincipal BenutzerDetails user,
                                            Model model) {
        if (result.hasErrors()) {
            MonatsbuchungVorschauDTO vorschau = buchungService.vorschauErstellen(user.getUserId());
            model.addAttribute("vorschau", vorschau);
            return "buchungen/monatsbuchung";
        }

        BuchungErgebnisDTO ergebnis = buchungService.monatsbuchungDurchfuehren(form, user.getUserId());
        model.addAttribute("ergebnis", ergebnis);
        return "buchungen/ergebnis";
    }

    // --- Manuelle Buchung ---

    @GetMapping("/manuell")
    public String manuellFormular(@AuthenticationPrincipal BenutzerDetails user, Model model) {
        List<SparkontoDTO> sparkonten = sparkontoService.findAllByUser(user.getUserId());
        model.addAttribute("sparkonten", sparkonten);
        model.addAttribute("buchungForm", new ManuelleBuchungFormDTO(null, null, LocalDate.now(), null));
        return "buchungen/manuell";
    }

    @PostMapping("/manuell")
    public String manuelleBuchungSpeichern(@Valid ManuelleBuchungFormDTO buchungForm,
                                           BindingResult result,
                                           @AuthenticationPrincipal BenutzerDetails user,
                                           Model model) {
        if (result.hasErrors()) {
            List<SparkontoDTO> sparkonten = sparkontoService.findAllByUser(user.getUserId());
            model.addAttribute("sparkonten", sparkonten);
            model.addAttribute("buchungForm", buchungForm);
            return "buchungen/manuell";
        }

        BuchungErgebnisDTO ergebnis = buchungService.manuelleBuchungSpeichern(buchungForm, user.getUserId());
        model.addAttribute("ergebnis", ergebnis);
        return "buchungen/ergebnis";
    }

    // --- Umbuchung ---

    @GetMapping("/umbuchung")
    public String umbuchungFormular(@AuthenticationPrincipal BenutzerDetails user, Model model) {
        List<SparkontoDTO> sparkonten = sparkontoService.findAllByUser(user.getUserId());
        model.addAttribute("sparkonten", sparkonten);
        model.addAttribute("umbuchungForm", new UmbuchungFormDTO(null, null, null, LocalDate.now(), null));
        return "buchungen/umbuchung";
    }

    @PostMapping("/umbuchung")
    public String umbuchungSpeichern(@Valid UmbuchungFormDTO umbuchungForm,
                                     BindingResult result,
                                     @AuthenticationPrincipal BenutzerDetails user,
                                     Model model) {
        if (result.hasErrors()) {
            List<SparkontoDTO> sparkonten = sparkontoService.findAllByUser(user.getUserId());
            model.addAttribute("sparkonten", sparkonten);
            model.addAttribute("umbuchungForm", umbuchungForm);
            return "buchungen/umbuchung";
        }

        try {
            BuchungErgebnisDTO ergebnis = buchungService.umbuchungSpeichern(umbuchungForm, user.getUserId());
            model.addAttribute("ergebnis", ergebnis);
            return "buchungen/ergebnis";
        } catch (GleichesKontoException ex) {
            List<SparkontoDTO> sparkonten = sparkontoService.findAllByUser(user.getUserId());
            model.addAttribute("sparkonten", sparkonten);
            model.addAttribute("umbuchungForm", umbuchungForm);
            model.addAttribute("gleichesKontoFehler", ex.getMessage());
            return "buchungen/umbuchung";
        }
    }

    // --- Buchungshistorie ---

    @GetMapping("/historie/{sparkontoId}")
    public String historie(@PathVariable Long sparkontoId,
                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate von,
                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate bis,
                           @RequestHeader(value = "HX-Request", required = false) String hxRequest,
                           @AuthenticationPrincipal BenutzerDetails user,
                           Model model) {
        BuchungshistorieDTO historie = buchungService.getHistorie(sparkontoId, von, bis, user.getUserId());
        model.addAttribute("historie", historie);
        model.addAttribute("von", von);
        model.addAttribute("bis", bis);

        if ("true".equals(hxRequest)) {
            return "buchungen/historie :: tabelle";
        }

        return "buchungen/historie";
    }
}
