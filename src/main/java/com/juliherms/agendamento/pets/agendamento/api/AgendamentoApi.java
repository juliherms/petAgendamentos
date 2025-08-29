package com.juliherms.agendamento.pets.agendamento.api;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;

public interface AgendamentoApi {

    /**
     * Request para criar um agendamento
     */
    record CreateAgendamentoRequest(
            @NotNull @Positive Long petId,
            @NotNull @Positive Long servicoId,
            @NotNull @Positive Long prestadorId,
            @NotNull LocalDate data,
            @NotNull LocalTime horaInicio
    ) {}

    /**
     * Response com dados do agendamento criado
     */
    record AgendamentoResponse(
            Long id,
            Long petId,
            Long servicoId,
            Long prestadorId,
            LocalDate data,
            LocalTime horaInicio,
            LocalTime horaFim,
            Status status,
            ZonedDateTime createdAt
    ) {}

    /**
     * Status do agendamento
     */
    enum Status {
        AGENDADO,
        CONFIRMADO,
        EM_ANDAMENTO,
        CONCLUIDO,
        CANCELADO
    }

    /**
     * Evento publicado quando um agendamento é criado
     */
    record AgendamentoCriadoEvent(
            Long agendamentoId,
            Long petId,
            Long servicoId,
            Long prestadorId,
            LocalDate data,
            LocalTime horaInicio,
            LocalTime horaFim
    ) {}
}
