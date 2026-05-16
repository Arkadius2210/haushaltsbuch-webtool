package de.haushaltsbuch.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ZugriffVerweigertException.class)
    public String handleZugriffVerweigert(ZugriffVerweigertException ex, Model model) {
        logger.warn("Zugriff verweigert: {}", ex.getMessage());
        model.addAttribute("message", ex.getMessage());
        return "error/403";
    }

    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception ex, Model model) {
        logger.error("Unerwarteter Fehler aufgetreten", ex);
        model.addAttribute("message", "Ein unerwarteter Fehler ist aufgetreten.");
        return "error/500";
    }
}
