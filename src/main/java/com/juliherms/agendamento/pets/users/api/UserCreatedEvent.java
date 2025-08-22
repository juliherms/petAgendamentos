package com.juliherms.agendamento.pets.users.api;

import java.time.Instant;

/**
 * Evento que representa a criação de um usuário.
 * Contém informações necessárias para o processo de verificação do usuário.
 */
public record UserCreatedEvent(
        Long userId,
        String email,
        String telefone,
        UserApi.CanalVerificacao canalPreferido,
        String tokenVerificacao,
        Instant expiresAt
) {}


