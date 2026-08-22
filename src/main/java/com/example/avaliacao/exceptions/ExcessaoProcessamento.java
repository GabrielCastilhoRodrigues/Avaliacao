package com.example.avaliacao.exceptions;

public class ExcessaoProcessamento extends RuntimeException {
    public ExcessaoProcessamento(String message) {
        super(message);
    }
}
