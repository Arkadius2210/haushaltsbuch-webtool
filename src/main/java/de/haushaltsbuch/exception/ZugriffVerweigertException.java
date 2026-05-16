package de.haushaltsbuch.exception;

public class ZugriffVerweigertException extends RuntimeException {

    public ZugriffVerweigertException() {
        super("Zugriff verweigert");
    }

    public ZugriffVerweigertException(String message) {
        super(message);
    }
}
