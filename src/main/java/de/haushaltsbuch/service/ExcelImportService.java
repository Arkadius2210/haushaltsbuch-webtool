package de.haushaltsbuch.service;

import de.haushaltsbuch.dto.ImportErgebnisDTO;
import de.haushaltsbuch.dto.ImportFormDTO;
import de.haushaltsbuch.dto.ImportVorschauDTO;

import java.io.InputStream;

public interface ExcelImportService {

    ImportVorschauDTO parseExcel(InputStream excelStream, Long userId);

    ImportErgebnisDTO importDurchfuehren(ImportFormDTO form, Long userId);
}
