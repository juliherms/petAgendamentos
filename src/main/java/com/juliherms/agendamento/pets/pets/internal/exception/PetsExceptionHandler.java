package com.juliherms.agendamento.pets.pets.internal.exception;

import com.juliherms.agendamento.pets.shared.exception.BusinessException;
import org.springframework.http.HttpStatus;

/**
 * Classe de tratamento de exceções específicas relacionadas aos pets.
 * Cada exceção estende a classe BusinessException com um código e status HTTP apropriados.
 */
public final class PetsExceptionHandler {

    private PetsExceptionHandler() {}

    public static class PetNaoEncontradoException extends BusinessException {
        public PetNaoEncontradoException(String mensagem) {
            super(mensagem, HttpStatus.NOT_FOUND, "PET-001");
        }
    }

    public static class DadosPetInvalidosException extends BusinessException {
        public DadosPetInvalidosException(String mensagem) {
            super(mensagem, HttpStatus.BAD_REQUEST, "PET-002");
        }
    }

    public static class LimitePetsExcedidoException extends BusinessException {
        public LimitePetsExcedidoException(String mensagem) {
            super(mensagem, HttpStatus.FORBIDDEN, "PET-003");
        }
    }
}
