package com.delivery.api.exception;

/** Lançada quando uma regra de negócio é violada (ex.: restaurante fechado, transição de status inválida). */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
