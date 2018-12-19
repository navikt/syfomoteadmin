package no.nav.syfo.service.exceptions;

public class MoteException extends RuntimeException {

    @SuppressWarnings("unused")
    public MoteException() {
    }

    public MoteException(String message) {
        super(message);
    }
}
