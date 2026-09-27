package com.jobtrack.exception;

public class ApplicationConflictException extends RuntimeException {
    public ApplicationConflictException(String message) {
        super(message);
    }

    public ApplicationConflictException(Long offerId) {
        super("Une candidature active existe déjà pour l'offre ID : " + offerId);
    }
}
