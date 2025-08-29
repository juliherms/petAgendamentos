package com.juliherms.agendamento.pets.agendamento.internal.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exceções específicas para o módulo de agendamento.
 */
public class AgendamentoExceptionHandler {

    /**
     * Exceção lançada quando o horário solicitado está fora do horário comercial.
     */
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public static class HorarioForaComercialException extends RuntimeException {
        public HorarioForaComercialException(String message) {
            super(message);
        }
    }

    /**
     * Exceção lançada quando o dia solicitado não é um dia de funcionamento.
     */
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public static class DiaIndisponivelException extends RuntimeException {
        public DiaIndisponivelException(String message) {
            super(message);
        }
    }

    /**
     * Exceção lançada quando o horário solicitado não está alinhado à hora cheia.
     */
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public static class HorarioNaoAlinhadoException extends RuntimeException {
        public HorarioNaoAlinhadoException(String message) {
            super(message);
        }
    }

    /**
     * Exceção lançada quando o horário solicitado está no passado.
     */
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public static class HorarioNoPassadoException extends RuntimeException {
        public HorarioNoPassadoException(String message) {
            super(message);
        }
    }

    /**
     * Exceção lançada quando o horário solicitado não está disponível (conflito).
     */
    @ResponseStatus(HttpStatus.CONFLICT)
    public static class HorarioIndisponivelException extends RuntimeException {
        public HorarioIndisponivelException(String message) {
            super(message);
        }
    }

    /**
     * Exceção lançada quando o pet não é encontrado.
     */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class PetNaoEncontradoException extends RuntimeException {
        public PetNaoEncontradoException(String message) {
            super(message);
        }
    }

    /**
     * Exceção lançada quando o serviço não é encontrado.
     */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class ServicoNaoEncontradoException extends RuntimeException {
        public ServicoNaoEncontradoException(String message) {
            super(message);
        }
    }

    /**
     * Exceção lançada quando o prestador não é encontrado.
     */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class PrestadorNaoEncontradoException extends RuntimeException {
        public PrestadorNaoEncontradoException(String message) {
            super(message);
        }
    }

    /**
     * Exceção lançada quando o usuário não tem permissão para agendar.
     */
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public static class UsuarioSemPermissaoException extends RuntimeException {
        public UsuarioSemPermissaoException(String message) {
            super(message);
        }
    }

    /**
     * Exceção lançada quando o agendamento não é encontrado.
     */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class AgendamentoNaoEncontradoException extends RuntimeException {
        public AgendamentoNaoEncontradoException(String message) {
            super(message);
        }
    }
}
