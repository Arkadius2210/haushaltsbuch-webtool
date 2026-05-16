package de.haushaltsbuch.service;

import de.haushaltsbuch.dto.ImportErgebnisDTO;
import de.haushaltsbuch.dto.ImportFehlerDTO;
import de.haushaltsbuch.dto.ImportFormDTO;
import de.haushaltsbuch.dto.ImportVorschauDTO;
import de.haushaltsbuch.dto.JahresausgabeFormDTO;
import de.haushaltsbuch.dto.SparkontoFormDTO;
import de.haushaltsbuch.model.Benutzer;
import de.haushaltsbuch.model.Jahresausgabe;
import de.haushaltsbuch.model.Sparkonto;
import de.haushaltsbuch.repository.BenutzerRepository;
import de.haushaltsbuch.repository.BuchungRepository;
import de.haushaltsbuch.repository.JahresausgabeRepository;
import de.haushaltsbuch.repository.SparkontoRepository;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ExcelImportServiceImpl implements ExcelImportService {

    private static final Logger log = LoggerFactory.getLogger(ExcelImportServiceImpl.class);

    private final SparkontoRepository sparkontoRepository;
    private final BuchungRepository buchungRepository;
    private final JahresausgabeRepository jahresausgabeRepository;
    private final BenutzerRepository benutzerRepository;

    public ExcelImportServiceImpl(SparkontoRepository sparkontoRepository,
                                  BuchungRepository buchungRepository,
                                  JahresausgabeRepository jahresausgabeRepository,
                                  BenutzerRepository benutzerRepository) {
        this.sparkontoRepository = sparkontoRepository;
        this.buchungRepository = buchungRepository;
        this.jahresausgabeRepository = jahresausgabeRepository;
        this.benutzerRepository = benutzerRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public ImportVorschauDTO parseExcel(InputStream excelStream, Long userId) {
        List<SparkontoFormDTO> sparkonten = new ArrayList<>();
        List<JahresausgabeFormDTO> jahresausgaben = new ArrayList<>();
        List<ImportFehlerDTO> fehler = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(excelStream)) {

            // Arbeitsblatt "Sparkonten" verarbeiten
            Sheet sparkontenSheet = workbook.getSheet("Sparkonten");
            if (sparkontenSheet != null) {
                for (int i = 1; i <= sparkontenSheet.getLastRowNum(); i++) {
                    Row row = sparkontenSheet.getRow(i);
                    if (row == null) {
                        continue;
                    }
                    try {
                        String name = getStringCellValue(row.getCell(0));
                        BigDecimal kontostand = getNumericCellValue(row.getCell(1));
                        BigDecimal standardbetrag = getNumericCellValue(row.getCell(2));

                        if (name == null || name.isBlank()) {
                            fehler.add(new ImportFehlerDTO("Sparkonten", i + 1, "Name ist leer"));
                            continue;
                        }

                        sparkonten.add(new SparkontoFormDTO(name.trim(), standardbetrag));
                    } catch (Exception e) {
                        fehler.add(new ImportFehlerDTO("Sparkonten", i + 1, e.getMessage()));
                    }
                }
            } else {
                fehler.add(new ImportFehlerDTO("Sparkonten", 0, "Arbeitsblatt nicht gefunden"));
            }

            // Arbeitsblatt "Einnahmen_Ausgaben" verarbeiten
            Sheet ausgabenSheet = workbook.getSheet("Einnahmen_Ausgaben");
            if (ausgabenSheet != null) {
                for (int i = 1; i <= ausgabenSheet.getLastRowNum(); i++) {
                    Row row = ausgabenSheet.getRow(i);
                    if (row == null) {
                        continue;
                    }
                    try {
                        String name = getStringCellValue(row.getCell(0));
                        BigDecimal betrag = getNumericCellValue(row.getCell(1));

                        if (name == null || name.isBlank()) {
                            fehler.add(new ImportFehlerDTO("Einnahmen_Ausgaben", i + 1, "Name ist leer"));
                            continue;
                        }

                        jahresausgaben.add(new JahresausgabeFormDTO(name.trim(), betrag));
                    } catch (Exception e) {
                        fehler.add(new ImportFehlerDTO("Einnahmen_Ausgaben", i + 1, e.getMessage()));
                    }
                }
            } else {
                fehler.add(new ImportFehlerDTO("Einnahmen_Ausgaben", 0, "Arbeitsblatt nicht gefunden"));
            }

        } catch (IOException e) {
            log.error("Fehler beim Lesen der Excel-Datei", e);
            fehler.add(new ImportFehlerDTO("Datei", 0, "Excel-Datei konnte nicht gelesen werden: " + e.getMessage()));
        }

        return new ImportVorschauDTO(sparkonten, jahresausgaben, fehler);
    }

    @Override
    public ImportErgebnisDTO importDurchfuehren(ImportFormDTO form, Long userId) {
        Benutzer benutzer = benutzerRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Benutzer nicht gefunden"));

        List<ImportFehlerDTO> fehler = new ArrayList<>();
        int sparkontenImportiert = 0;
        int jahresausgabenImportiert = 0;

        // Sparkonten importieren
        if (form.sparkonten() != null) {
            for (int i = 0; i < form.sparkonten().size(); i++) {
                SparkontoFormDTO sparkontoForm = form.sparkonten().get(i);
                try {
                    Sparkonto sparkonto = new Sparkonto();
                    sparkonto.setName(sparkontoForm.name());
                    sparkonto.setStandardbetrag(sparkontoForm.standardbetrag());
                    sparkonto.setBenutzer(benutzer);
                    sparkonto.setErstelltAm(LocalDateTime.now());

                    sparkontoRepository.save(sparkonto);
                    sparkontenImportiert++;
                } catch (Exception e) {
                    fehler.add(new ImportFehlerDTO("Sparkonten", i + 1, e.getMessage()));
                }
            }
        }

        // Jahresausgaben importieren
        if (form.jahresausgaben() != null) {
            for (int i = 0; i < form.jahresausgaben().size(); i++) {
                JahresausgabeFormDTO jahresausgabeForm = form.jahresausgaben().get(i);
                try {
                    Jahresausgabe jahresausgabe = new Jahresausgabe();
                    jahresausgabe.setName(jahresausgabeForm.name());
                    jahresausgabe.setJahresbetrag(jahresausgabeForm.jahresbetrag());
                    jahresausgabe.setBenutzer(benutzer);
                    jahresausgabe.setErstelltAm(LocalDateTime.now());

                    jahresausgabeRepository.save(jahresausgabe);
                    jahresausgabenImportiert++;
                } catch (Exception e) {
                    fehler.add(new ImportFehlerDTO("Einnahmen_Ausgaben", i + 1, e.getMessage()));
                }
            }
        }

        return new ImportErgebnisDTO(sparkontenImportiert, jahresausgabenImportiert, fehler);
    }

    private String getStringCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue();
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            return String.valueOf((long) cell.getNumericCellValue());
        }
        return null;
    }

    private BigDecimal getNumericCellValue(Cell cell) {
        if (cell == null) {
            return BigDecimal.ZERO;
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            return BigDecimal.valueOf(cell.getNumericCellValue());
        }
        if (cell.getCellType() == CellType.STRING) {
            String value = cell.getStringCellValue().trim();
            if (value.isEmpty()) {
                return BigDecimal.ZERO;
            }
            return new BigDecimal(value.replace(",", "."));
        }
        return BigDecimal.ZERO;
    }
}
