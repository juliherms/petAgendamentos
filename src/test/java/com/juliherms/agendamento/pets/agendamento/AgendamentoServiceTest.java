package com.juliherms.agendamento.pets.agendamento;

import com.juliherms.agendamento.pets.agendamento.api.AgendamentoApi;
import com.juliherms.agendamento.pets.agendamento.internal.domain.Agendamento;
import com.juliherms.agendamento.pets.agendamento.internal.domain.ConfiguracaoHorario;
import com.juliherms.agendamento.pets.agendamento.internal.exception.AgendamentoExceptionHandler;
import com.juliherms.agendamento.pets.agendamento.internal.repo.AgendamentoRepository;
import com.juliherms.agendamento.pets.agendamento.internal.repo.ConfiguracaoHorarioRepository;
import com.juliherms.agendamento.pets.agendamento.internal.service.AgendamentoService;
import com.juliherms.agendamento.pets.pets.internal.domain.Pet;
import com.juliherms.agendamento.pets.pets.internal.repo.PetRepository;
import com.juliherms.agendamento.pets.services.internal.domain.OfferedService;
import com.juliherms.agendamento.pets.services.internal.repo.OfferedServiceRepository;
import com.juliherms.agendamento.pets.users.api.UserApi;
import com.juliherms.agendamento.pets.users.internal.domain.User;
import com.juliherms.agendamento.pets.users.internal.repo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class AgendamentoServiceTest {

    @Mock
    private AgendamentoRepository agendamentoRepository;

    @Mock
    private ConfiguracaoHorarioRepository configuracaoHorarioRepository;

    @Mock
    private PetRepository petRepository;

    @Mock
    private OfferedServiceRepository servicoRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private AgendamentoService agendamentoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldCreateAgendamentoSuccessfully() {
        // Arrange
        AgendamentoApi.CreateAgendamentoRequest request = new AgendamentoApi.CreateAgendamentoRequest(
                1L, 2L, 3L, LocalDate.now().plusDays(1), LocalTime.of(10, 0)
        );

        when(petRepository.findById(1L)).thenReturn(Optional.of(mockPet()));
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser(UserApi.Status.ativo)));
        when(servicoRepository.findById(2L)).thenReturn(Optional.of(mockServico(true)));
        when(userRepository.findById(3L)).thenReturn(Optional.of(mockPrestador(UserApi.Status.ativo, UserApi.Perfil.PROVEDOR)));
        when(configuracaoHorarioRepository.findByDiaSemanaAndAtivoTrue(any())).thenReturn(Optional.of(mockConfiguracaoHorario()));
        when(agendamentoRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        AgendamentoApi.AgendamentoResponse response = agendamentoService.criarAgendamento(request);

        // Assert
        assertThat(response).isNotNull();
        verify(agendamentoRepository).save(any(Agendamento.class));
        verify(eventPublisher).publishEvent(any(AgendamentoApi.AgendamentoCriadoEvent.class));
    }

    @Test
    void shouldThrowExceptionWhenPetNotFound() {
        // Arrange
        AgendamentoApi.CreateAgendamentoRequest request = new AgendamentoApi.CreateAgendamentoRequest(
                1L, 2L, 3L, LocalDate.now().plusDays(1), LocalTime.of(10, 0)
        );

        when(petRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> agendamentoService.criarAgendamento(request))
                .isInstanceOf(AgendamentoExceptionHandler.PetNaoEncontradoException.class)
                .hasMessageContaining("Pet não encontrado com ID: 1");
    }

    @Test
    void shouldFetchAgendamentosByUsuario() {
        // Arrange
        when(agendamentoRepository.findByUsuarioId(1L)).thenReturn(mockAgendamentos());

        // Act
        var agendamentos = agendamentoService.buscarAgendamentosPorUsuario(1L);

        // Assert
        assertThat(agendamentos).isNotEmpty();
        verify(agendamentoRepository).findByUsuarioId(1L);
    }

    @Test
    void shouldFetchAgendamentosByPet() {
        // Arrange
        when(agendamentoRepository.findByPetIdOrderByDataDescHoraInicioDesc(1L)).thenReturn(mockAgendamentos());

        // Act
        var agendamentos = agendamentoService.buscarAgendamentosPorPet(1L);

        // Assert
        assertThat(agendamentos).isNotEmpty();
        verify(agendamentoRepository).findByPetIdOrderByDataDescHoraInicioDesc(1L);
    }

    // Mock helpers
    private Agendamento mockAgendamento() {
        Agendamento agendamento = new Agendamento();
        agendamento.setId(1L);
        agendamento.setPetId(1L);
        agendamento.setServicoId(2L);
        agendamento.setPrestadorId(3L);
        return agendamento;
    }

    private java.util.List<Agendamento> mockAgendamentos() {
        return java.util.List.of(mockAgendamento());
    }

    private User mockUser(UserApi.Status status) {
        User user = new User();
        user.setStatus(status);
        return user;
    }

    private User mockPrestador(UserApi.Status status, UserApi.Perfil perfil) {
        User user = mockUser(status);
        user.setPerfil(perfil);
        return user;
    }

    private OfferedService mockServico(boolean ativo) {
        OfferedService servico = new OfferedService();
        servico.setAtivo(ativo);
        return servico;
    }

    private ConfiguracaoHorario mockConfiguracaoHorario() {
        ConfiguracaoHorario config = new ConfiguracaoHorario();
        config.setHoraAbertura(LocalTime.of(8, 0));
        config.setHoraFechamento(LocalTime.of(18, 0));
        return config;
    }

    private Pet mockPet() {
        Pet pet = new Pet();
        pet.setUsuarioId(1L);
        return pet;
    }
}