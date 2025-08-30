package com.juliherms.agendamento.pets.agendamento.internal.service;

import com.juliherms.agendamento.pets.agendamento.api.AgendamentoApi;

import com.juliherms.agendamento.pets.agendamento.internal.domain.Agendamento;
import com.juliherms.agendamento.pets.agendamento.internal.exception.AgendamentoExceptionHandler;
import com.juliherms.agendamento.pets.agendamento.internal.repo.AgendamentoRepository;
import com.juliherms.agendamento.pets.agendamento.internal.repo.ConfiguracaoHorarioRepository;
import com.juliherms.agendamento.pets.pets.internal.repo.PetRepository;
import com.juliherms.agendamento.pets.services.internal.repo.OfferedServiceRepository;
import com.juliherms.agendamento.pets.users.api.UserApi;
import com.juliherms.agendamento.pets.users.internal.repo.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Serviço responsável pela criação e validação de agendamentos.
 * Implementa todas as regras de negócio para agendamento de serviços.
 */
@Service
public class AgendamentoService {

    private static final Logger log = LoggerFactory.getLogger(AgendamentoService.class);

    private final AgendamentoRepository agendamentoRepository;
    private final ConfiguracaoHorarioRepository configuracaoHorarioRepository;
    private final PetRepository petRepository;
    private final OfferedServiceRepository servicoRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    public AgendamentoService(
            AgendamentoRepository agendamentoRepository,
            ConfiguracaoHorarioRepository configuracaoHorarioRepository,
            PetRepository petRepository,
            OfferedServiceRepository servicoRepository,
            UserRepository userRepository,
            ApplicationEventPublisher eventPublisher) {
        this.agendamentoRepository = agendamentoRepository;
        this.configuracaoHorarioRepository = configuracaoHorarioRepository;
        this.petRepository = petRepository;
        this.servicoRepository = servicoRepository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Cria um novo agendamento validando todas as regras de negócio.
     * 
     * @param request Dados do agendamento a ser criado
     * @return Response com os dados do agendamento criado
     */
    @Transactional
    public AgendamentoApi.AgendamentoResponse criarAgendamento(AgendamentoApi.CreateAgendamentoRequest request) {

        log.info("[AGENDAMENTO] Iniciando criação de agendamento para pet {}, serviço {}, prestador {}",
                request.petId(), request.servicoId(), request.prestadorId());

        // Validações de negócio
        validarPet(request.petId());
        validarServico(request.servicoId());
        validarPrestador(request.prestadorId());
        validarHorario(request.data(), request.horaInicio());
        validarDisponibilidade(request.prestadorId(), request.data(), request.horaInicio());

        // Cria o agendamento
        Agendamento agendamento = new Agendamento();
        agendamento.setPetId(request.petId());
        agendamento.setServicoId(request.servicoId());
        agendamento.setPrestadorId(request.prestadorId());
        agendamento.setData(request.data());
        agendamento.setHoraInicio(request.horaInicio());

        Agendamento saved = agendamentoRepository.save(agendamento);

        log.info("[AGENDAMENTO] Agendamento criado com sucesso: ID {}", saved.getId());

        // Publica evento de agendamento criado
        AgendamentoApi.AgendamentoCriadoEvent event = new AgendamentoApi.AgendamentoCriadoEvent(
                saved.getId(),
                saved.getPetId(),
                saved.getServicoId(),
                saved.getPrestadorId(),
                saved.getData(),
                saved.getHoraInicio(),
                saved.getHoraFim()
        );
        eventPublisher.publishEvent(event);

        // Retorna response
        return new AgendamentoApi.AgendamentoResponse(
                saved.getId(),
                saved.getPetId(),
                saved.getServicoId(),
                saved.getPrestadorId(),
                saved.getData(),
                saved.getHoraInicio(),
                saved.getHoraFim(),
                saved.getStatus(),
                saved.getCreatedAt()
        );
    }

    /**
     * Valida se o pet existe e está ativo.
     */
    private void validarPet(Long petId) {
        var pet = petRepository.findById(petId)
                .orElseThrow(() -> new AgendamentoExceptionHandler.PetNaoEncontradoException(
                        "Pet não encontrado com ID: " + petId));

        // Verifica se o pet pertence a um usuário ativo
        var user = userRepository.findById(pet.getUsuarioId())
                .orElseThrow(() -> new AgendamentoExceptionHandler.UsuarioSemPermissaoException(
                        "Usuário do pet não encontrado"));

        if (user.getStatus() != UserApi.Status.ativo) {
            throw new AgendamentoExceptionHandler.UsuarioSemPermissaoException(
                    "Usuário do pet não está ativo");
        }
    }

    /**
     * Valida se o serviço existe e está ativo.
     */
    private void validarServico(Long servicoId) {
        var servico = servicoRepository.findById(servicoId)
                .orElseThrow(() -> new AgendamentoExceptionHandler.ServicoNaoEncontradoException(
                        "Serviço não encontrado com ID: " + servicoId));

        if (!servico.isAtivo()) {
            throw new AgendamentoExceptionHandler.ServicoNaoEncontradoException(
                    "Serviço não está ativo");
        }
    }

    /**
     * Valida se o prestador existe e está ativo.
     */
    private void validarPrestador(Long prestadorId) {
        var prestador = userRepository.findById(prestadorId)
                .orElseThrow(() -> new AgendamentoExceptionHandler.PrestadorNaoEncontradoException(
                        "Prestador não encontrado com ID: " + prestadorId));

        if (prestador.getStatus() != UserApi.Status.ativo) {
            throw new AgendamentoExceptionHandler.PrestadorNaoEncontradoException(
                    "Prestador não está ativo");
        }

        if (prestador.getPerfil() != UserApi.Perfil.PROVEDOR) {
            throw new AgendamentoExceptionHandler.PrestadorNaoEncontradoException(
                    "Usuário não é um prestador de serviços");
        }
    }

    /**
     * Valida se o horário solicitado está dentro das regras de negócio.
     */
    private void validarHorario(LocalDate data, LocalTime horaInicio) {
        ZoneId zoneId = ZoneId.of("America/Recife");
        ZonedDateTime agora = ZonedDateTime.now(zoneId);
        ZonedDateTime horarioSolicitado = data.atTime(horaInicio).atZone(zoneId);

        // Valida se não está no passado
        if (horarioSolicitado.isBefore(agora)) {
            throw new AgendamentoExceptionHandler.HorarioNoPassadoException(
                    "Data/hora no passado");
        }

        // Valida se é hoje e o horário já passou
        if (data.equals(agora.toLocalDate()) && horaInicio.isBefore(agora.toLocalTime())) {
            throw new AgendamentoExceptionHandler.HorarioNoPassadoException(
                    "Horário já passou para hoje");
        }

        // Valida alinhamento à hora cheia
        if (horaInicio.getMinute() != 0 || horaInicio.getSecond() != 0 || horaInicio.getNano() != 0) {
            throw new AgendamentoExceptionHandler.HorarioNaoAlinhadoException(
                    "Horário inválido; use horas cheias (ex: 09:00, 10:00)");
        }

        // Valida dia da semana
        DayOfWeek diaSemana = data.getDayOfWeek();
        if (diaSemana == DayOfWeek.SUNDAY) {
            throw new AgendamentoExceptionHandler.DiaIndisponivelException(
                    "Domingo não é dia de funcionamento");
        }

        // Valida horário comercial
        var configHorario = configuracaoHorarioRepository.findByDiaSemanaAndAtivoTrue(diaSemana)
                .orElseThrow(() -> new AgendamentoExceptionHandler.DiaIndisponivelException(
                        "Dia " + diaSemana + " não configurado para funcionamento"));

        if (horaInicio.isBefore(configHorario.getHoraAbertura()) || 
            horaInicio.isAfter(configHorario.getHoraFechamento()) ||
            horaInicio.equals(configHorario.getHoraFechamento())) {
            throw new AgendamentoExceptionHandler.HorarioForaComercialException(
                    "Horário fora do horário comercial (" + 
                    configHorario.getHoraAbertura() + " - " + 
                    configHorario.getHoraFechamento() + ")");
        }
    }

    /**
     * Valida se o horário está disponível para o prestador.
     */
    private void validarDisponibilidade(Long prestadorId, LocalDate data, LocalTime horaInicio) {
        var conflito = agendamentoRepository.findByPrestadorIdAndDataAndHoraInicio(
                prestadorId, data, horaInicio);

        if (conflito.isPresent()) {
            throw new AgendamentoExceptionHandler.HorarioIndisponivelException(
                    "Horário indisponível para o prestador");
        }
    }

    /**
     * Busca agendamentos por usuário (tutor do pet).
     */
    public java.util.List<AgendamentoApi.AgendamentoResponse> buscarAgendamentosPorUsuario(Long usuarioId) {
        var agendamentos = agendamentoRepository.findByUsuarioId(usuarioId);
        
        return agendamentos.stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Busca agendamentos por pet.
     */
    public java.util.List<AgendamentoApi.AgendamentoResponse> buscarAgendamentosPorPet(Long petId) {
        var agendamentos = agendamentoRepository.findByPetIdOrderByDataDescHoraInicioDesc(petId);
        
        return agendamentos.stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Mapeia entidade para response.
     */
    private AgendamentoApi.AgendamentoResponse mapToResponse(Agendamento agendamento) {
        return new AgendamentoApi.AgendamentoResponse(
                agendamento.getId(),
                agendamento.getPetId(),
                agendamento.getServicoId(),
                agendamento.getPrestadorId(),
                agendamento.getData(),
                agendamento.getHoraInicio(),
                agendamento.getHoraFim(),
                agendamento.getStatus(),
                agendamento.getCreatedAt()
        );
    }
}

