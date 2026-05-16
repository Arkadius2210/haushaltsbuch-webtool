package de.haushaltsbuch.controller;

import de.haushaltsbuch.dto.BenutzerDetails;
import de.haushaltsbuch.dto.ImportErgebnisDTO;
import de.haushaltsbuch.dto.ImportFormDTO;
import de.haushaltsbuch.dto.ImportVorschauDTO;
import de.haushaltsbuch.service.ExcelImportService;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@Controller
@RequestMapping("/import")
public class ImportController {

    private final ExcelImportService excelImportService;

    public ImportController(ExcelImportService excelImportService) {
        this.excelImportService = excelImportService;
    }

    @GetMapping
    public String uploadForm() {
        return "import/upload";
    }

    @PostMapping("/vorschau")
    public String importVorschau(@RequestParam("datei") MultipartFile datei,
                                 @AuthenticationPrincipal BenutzerDetails user,
                                 Model model) {
        if (datei.isEmpty()) {
            model.addAttribute("fehler", "Bitte wählen Sie eine Datei aus.");
            return "import/upload";
        }

        try {
            ImportVorschauDTO vorschau = excelImportService.parseExcel(datei.getInputStream(), user.getUserId());
            model.addAttribute("vorschau", vorschau);
            return "import/vorschau";
        } catch (IOException e) {
            model.addAttribute("fehler", "Fehler beim Lesen der Datei: " + e.getMessage());
            return "import/upload";
        }
    }

    @PostMapping("/bestaetigen")
    public String importBestaetigen(ImportFormDTO form,
                                    @AuthenticationPrincipal BenutzerDetails user,
                                    RedirectAttributes redirectAttributes) {
        ImportErgebnisDTO ergebnis = excelImportService.importDurchfuehren(form, user.getUserId());
        redirectAttributes.addFlashAttribute("erfolg",
                String.format("Import erfolgreich: %d Sparkonten und %d Jahresausgaben importiert.",
                        ergebnis.sparkontenImportiert(), ergebnis.jahresausgabenImportiert()));
        return "redirect:/sparkonten";
    }
}
