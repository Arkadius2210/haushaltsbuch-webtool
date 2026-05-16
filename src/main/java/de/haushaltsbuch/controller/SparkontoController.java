package de.haushaltsbuch.controller;

import de.haushaltsbuch.dto.BenutzerDetails;
import de.haushaltsbuch.dto.SparkontoDTO;
import de.haushaltsbuch.dto.SparkontoFormDTO;
import de.haushaltsbuch.service.SparkontoService;

import jakarta.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/sparkonten")
public class SparkontoController {

    private final SparkontoService sparkontoService;

    public SparkontoController(SparkontoService sparkontoService) {
        this.sparkontoService = sparkontoService;
    }

    @GetMapping
    public String index(@AuthenticationPrincipal BenutzerDetails user, Model model) {
        Long userId = user.getUserId();
        List<SparkontoDTO> sparkonten = sparkontoService.findAllByUser(userId);
        BigDecimal gesamtsaldo = sparkontoService.berechneGesamtsaldo(userId);
        BigDecimal monatlichGesamtbedarf = sparkontoService.berechneMonatlicherGesamtbedarf(userId);

        model.addAttribute("sparkonten", sparkonten);
        model.addAttribute("gesamtsaldo", gesamtsaldo);
        model.addAttribute("monatlichGesamtbedarf", monatlichGesamtbedarf);

        return "sparkonten/index";
    }

    @GetMapping("/neu")
    public String neu(Model model) {
        model.addAttribute("sparkontoForm", new SparkontoFormDTO("", BigDecimal.ZERO));
        return "sparkonten/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("sparkontoForm") SparkontoFormDTO sparkontoForm,
                         BindingResult result,
                         @AuthenticationPrincipal BenutzerDetails user,
                         Model model) {
        if (result.hasErrors()) {
            return "sparkonten/form";
        }

        sparkontoService.create(sparkontoForm, user.getUserId());
        return "redirect:/sparkonten";
    }

    @GetMapping("/{id}/bearbeiten")
    public String bearbeiten(@PathVariable Long id,
                             @AuthenticationPrincipal BenutzerDetails user,
                             Model model) {
        List<SparkontoDTO> sparkonten = sparkontoService.findAllByUser(user.getUserId());
        SparkontoDTO sparkonto = sparkonten.stream()
                .filter(s -> s.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Sparkonto nicht gefunden"));

        model.addAttribute("sparkontoForm", new SparkontoFormDTO(sparkonto.name(), sparkonto.standardbetrag()));
        model.addAttribute("sparkontoId", id);
        return "sparkonten/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("sparkontoForm") SparkontoFormDTO sparkontoForm,
                         BindingResult result,
                         @AuthenticationPrincipal BenutzerDetails user,
                         Model model) {
        if (result.hasErrors()) {
            model.addAttribute("sparkontoId", id);
            return "sparkonten/form";
        }

        sparkontoService.update(id, sparkontoForm, user.getUserId());
        return "redirect:/sparkonten";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         @AuthenticationPrincipal BenutzerDetails user) {
        sparkontoService.delete(id, user.getUserId());
        return "redirect:/sparkonten";
    }
}
