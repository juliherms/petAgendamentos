package com.juliherms.agendamento.pets.agendamento.internal.repo;

import com.juliherms.agendamento.pets.agendamento.internal.domain.ConfiguracaoHorario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

/**
 * Repositório para configurações de horário comercial.
 */
@Repository
public interface ConfiguracaoHorarioRepository extends JpaRepository<ConfiguracaoHorario, Long> {

    /**
     * Busca configuração de horário para um dia específico da semana.
     */
    Optional<ConfiguracaoHorario> findByDiaSemanaAndAtivoTrue(DayOfWeek diaSemana);

    /**
     * Busca todas as configurações ativas.
     */
    List<ConfiguracaoHorario> findByAtivoTrueOrderByDiaSemana();

    /**
     * Verifica se existe configuração para um dia específico.
     */
    boolean existsByDiaSemanaAndAtivoTrue(DayOfWeek diaSemana);
}
