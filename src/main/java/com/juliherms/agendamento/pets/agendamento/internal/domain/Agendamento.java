package com.juliherms.agendamento.pets.agendamento.internal.domain;

import com.juliherms.agendamento.pets.agendamento.api.AgendamentoApi.Status;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Entidade que representa um agendamento de serviço para pet.
 * Cada agendamento ocupa um slot de 1 hora a partir do horário de início.
 */
@Entity
@Table(name = "agendamentos", 
       indexes = {
           @Index(name = "idx_agendamento_prestador_data_hora", 
                  columnList = "prestadorId, data, horaInicio"),
           @Index(name = "idx_agendamento_pet", 
                  columnList = "petId"),
           @Index(name = "idx_agendamento_servico", 
                  columnList = "servicoId")
       })
public class Agendamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pet_id", nullable = false)
    private Long petId;

    @Column(name = "servico_id", nullable = false)
    private Long servicoId;

    @Column(name = "prestador_id", nullable = false)
    private Long prestadorId;

    @Column(name = "data", nullable = false)
    private LocalDate data;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fim", nullable = false)
    private LocalTime horaFim;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.AGENDADO;

    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    @PrePersist
    void prePersist() {
        ZoneId zoneId = ZoneId.of("America/Recife");
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        createdAt = now;
        updatedAt = now;
        
        // Calcula hora fim (1 hora após início)
        if (horaInicio != null) {
            horaFim = horaInicio.plusHours(1);
        }
    }

    @PreUpdate
    void preUpdate() {
        ZoneId zoneId = ZoneId.of("America/Recife");
        updatedAt = ZonedDateTime.now(zoneId);
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPetId() {
        return petId;
    }

    public void setPetId(Long petId) {
        this.petId = petId;
    }

    public Long getServicoId() {
        return servicoId;
    }

    public void setServicoId(Long servicoId) {
        this.servicoId = servicoId;
    }

    public Long getPrestadorId() {
        return prestadorId;
    }

    public void setPrestadorId(Long prestadorId) {
        this.prestadorId = prestadorId;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
        if (horaInicio != null) {
            this.horaFim = horaInicio.plusHours(1);
        }
    }

    public LocalTime getHoraFim() {
        return horaFim;
    }

    public void setHoraFim(LocalTime horaFim) {
        this.horaFim = horaFim;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(ZonedDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

