package de.haushaltsbuch.controller;

import de.haushaltsbuch.dto.BenutzerDetails;
import de.haushaltsbuch.dto.JahresausgabeDTO;
import de.haushaltsbuch.dto.JahresausgabeFormDTO;
import de.haushaltsbuch.service.JahresausgabeService;

import jakarta.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/jahresausgaben")
public class JahresausgabeController {

    private final JahresausgabeService jahresausgabeService;

    public JahresausgabeController(JahresausgabeService jahresausgabeService) {
        this.jahresausgabeService = jahresausgabeService;
    }

    @GetMapping
    public String index(@AuthenticationPrincipal BenutzerDetails user, Model model) {
        Long userId = user.getUserId();
        List<JahresausgabeDTO> jahresausgaben = jahresausgabeService.findAllByUser(userId);
        BigDecimal gesamtMonatlicheRuecklage = jahresausgabeService.berechneGesamtMonatlicheRuecklage(userId);

        model.addAttribute("jahresausgaben", jahresausgaben);
        model.addAttribute("gesamtMonatlicheRuecklage", gesamtMonatlicheRuecklage);

        return "jahresausgaben/index";
    }

    @GetMapping("/neu")
    public String neu(Model model) {
        model.addAttribute("jahresausgabeForm", new JahresausgabeFormDTO("", null));
        return "jahresausgaben/form";
    }

    @PostMapping
    public String create(@Valid JahresausgabeFormDTO jahresausgabeForm,
                         BindingResult result,
                         @AuthenticationPrincipal BenutzerDetails user,
                         Model model) {
        if (result.hasErrors()) {
            model.addAttribute("jahresausgabeForm", jahresausgabeForm);
            return "jahresausgaben/form";
        }

        jahresausgabeService.create(jahresausgabeForm, user.getUserId());
        return "redirect:/jahresausgaben";
    }

    @GetMapping("/{id}/bearbeiten")
    public String bearbeiten(@PathVariable Long id,
                             @AuthenticationPrincipal BenutzerDetails user,
                             Model model) {
        List<JahresausgabeDTO> jahresausgaben = jahresausgabeService.findAllByUser(user.getUserId());
        JahresausgabeDTO jahresausgabe = jahresausgaben.stream()
                .filter(j -> j.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Jahresausgabe nicht gefunden"));

        model.addAttribute("jahresausgabeForm", new JahresausgabeFormDTO(jahresausgabe.name(), jahresausgabe.jahresbetrag()));
        model.addAttribute("jahresausgabeId", id);
        return "jahresausgaben/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid JahresausgabeFormDTO jahresausgabeForm,
                         BindingResult result,
                         @AuthenticationPrincipal BenutzerDetails user,
                         Model model) {
        if (result.hasErrors()) {
            model.addAttribute("jahresausgabeForm", jahresausgabeForm);
            model.addAttribute("jahresausgabeId", id);
            return "jahresausgaben/form";
        }

        jahresausgabeService.update(id, jahresausgabeForm, user.getUserId());
        return "redirect:/jahresausgaben";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         @AuthenticationPrincipal BenutzerDetails user) {
        jahresausgabeService.delete(id, user.getUserId());
        return "redirect:/jahresausgaben";
    }
}
