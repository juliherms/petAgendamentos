package com.juliherms.agendamento.pets.agendamento.internal.repo;

import com.juliherms.agendamento.pets.agendamento.internal.domain.Agendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositório para operações de agendamento.
 * Inclui métodos para verificar disponibilidade de horários.
 */
@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

    /**
     * Verifica se existe agendamento conflitante para o mesmo prestador,
     * data e horário de início.
     */
    @Query("SELECT a FROM Agendamento a WHERE a.prestadorId = :prestadorId " +
           "AND a.data = :data AND a.horaInicio = :horaInicio " +
           "AND a.status NOT IN ('CANCELADO')")
    Optional<Agendamento> findByPrestadorIdAndDataAndHoraInicio(
            @Param("prestadorId") Long prestadorId,
            @Param("data") LocalDate data,
            @Param("horaInicio") LocalTime horaInicio
    );

    /**
     * Busca agendamentos por prestador e data.
     */
    @Query("SELECT a FROM Agendamento a WHERE a.prestadorId = :prestadorId " +
           "AND a.data = :data AND a.status NOT IN ('CANCELADO') " +
           "ORDER BY a.horaInicio")
    List<Agendamento> findByPrestadorIdAndData(
            @Param("prestadorId") Long prestadorId,
            @Param("data") LocalDate data
    );

    /**
     * Busca agendamentos por pet.
     */
    List<Agendamento> findByPetIdOrderByDataDescHoraInicioDesc(Long petId);

    /**
     * Busca agendamentos por usuário (tutor do pet).
     */
    @Query("SELECT a FROM Agendamento a JOIN Pet p ON a.petId = p.id " +
           "WHERE p.usuarioId = :usuarioId ORDER BY a.data DESC, a.horaInicio DESC")
    List<Agendamento> findByUsuarioId(@Param("usuarioId") Long usuarioId);

    /**
     * Verifica se existe agendamento ativo para o pet.
     */
    @Query("SELECT COUNT(a) > 0 FROM Agendamento a WHERE a.petId = :petId " +
           "AND a.status IN ('AGENDADO', 'CONFIRMADO', 'EM_ANDAMENTO')")
    boolean existsByPetIdAndStatusAtivo(@Param("petId") Long petId);
}
