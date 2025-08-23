package com.juliherms.agendamento.pets.users.internal.repo;

import com.juliherms.agendamento.pets.users.api.UserApi;
import com.juliherms.agendamento.pets.users.internal.domain.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findTopByIdUsuarioAndCanalAndUtilizadoIsFalseAndExpiresAtAfterOrderByExpiresAtDesc(Long userId,
                                                                                                                   UserApi.CanalVerificacao canal,
                                                                                                                   Instant now);
}


