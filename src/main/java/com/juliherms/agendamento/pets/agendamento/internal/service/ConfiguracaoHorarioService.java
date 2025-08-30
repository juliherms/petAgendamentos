package com.juliherms.agendamento.pets.agendamento.internal.service;

import com.juliherms.agendamento.pets.agendamento.internal.domain.ConfiguracaoHorario;
import com.juliherms.agendamento.pets.agendamento.internal.repo.ConfiguracaoHorarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Serviço para inicializar configurações de horário comercial padrão.
 * Executa na inicialização da aplicação para garantir que as configurações básicas existam.
 */
@Service
public class ConfiguracaoHorarioService implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ConfiguracaoHorarioService.class);

    private final ConfiguracaoHorarioRepository configuracaoHorarioRepository;

    public ConfiguracaoHorarioService(ConfiguracaoHorarioRepository configuracaoHorarioRepository) {
        this.configuracaoHorarioRepository = configuracaoHorarioRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("[CONFIGURACAO] Inicializando configurações de horário comercial padrão");
        
        inicializarConfiguracoesPadrao();
        
        log.info("[CONFIGURACAO] Configurações de horário comercial inicializadas com sucesso");
    }

    /**
     * Inicializa as configurações de horário comercial padrão.
     * Segunda a Sábado: 09:00 - 18:00
     * Domingo: não funciona
     */
    private void inicializarConfiguracoesPadrao() {
        // Segunda-feira
        criarOuAtualizarConfiguracao(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(18, 0));
        
        // Terça-feira
        criarOuAtualizarConfiguracao(DayOfWeek.TUESDAY, LocalTime.of(9, 0), LocalTime.of(18, 0));
        
        // Quarta-feira
        criarOuAtualizarConfiguracao(DayOfWeek.WEDNESDAY, LocalTime.of(9, 0), LocalTime.of(18, 0));
        
        // Quinta-feira
        criarOuAtualizarConfiguracao(DayOfWeek.THURSDAY, LocalTime.of(9, 0), LocalTime.of(18, 0));
        
        // Sexta-feira
        criarOuAtualizarConfiguracao(DayOfWeek.FRIDAY, LocalTime.of(9, 0), LocalTime.of(18, 0));
        
        // Sábado
        criarOuAtualizarConfiguracao(DayOfWeek.SATURDAY, LocalTime.of(9, 0), LocalTime.of(18, 0));
        
        // Domingo - não configurado (não funciona)
        log.info("[CONFIGURACAO] Domingo configurado como dia não funcionante");
    }

    /**
     * Cria ou atualiza uma configuração de horário para um dia específico.
     */
    private void criarOuAtualizarConfiguracao(DayOfWeek diaSemana, LocalTime horaAbertura, LocalTime horaFechamento) {
        var configExistente = configuracaoHorarioRepository.findByDiaSemanaAndAtivoTrue(diaSemana);
        
        if (configExistente.isPresent()) {
            var config = configExistente.get();
            config.setHoraAbertura(horaAbertura);
            config.setHoraFechamento(horaFechamento);
            configuracaoHorarioRepository.save(config);
            log.debug("[CONFIGURACAO] Configuração atualizada para {}: {} - {}", 
                     diaSemana, horaAbertura, horaFechamento);
        } else {
            var novaConfig = new ConfiguracaoHorario();
            novaConfig.setDiaSemana(diaSemana);
            novaConfig.setHoraAbertura(horaAbertura);
            novaConfig.setHoraFechamento(horaFechamento);
            novaConfig.setAtivo(true);
            configuracaoHorarioRepository.save(novaConfig);
            log.debug("[CONFIGURACAO] Nova configuração criada para {}: {} - {}", 
                     diaSemana, horaAbertura, horaFechamento);
        }
    }

    /**
     * Busca configuração de horário para um dia específico.
     */
    public ConfiguracaoHorario buscarConfiguracao(DayOfWeek diaSemana) {
        return configuracaoHorarioRepository.findByDiaSemanaAndAtivoTrue(diaSemana)
                .orElseThrow(() -> new RuntimeException("Configuração de horário não encontrada para " + diaSemana));
    }

    /**
     * Verifica se um dia está configurado para funcionamento.
     */
    public boolean diaFunciona(DayOfWeek diaSemana) {
        return configuracaoHorarioRepository.existsByDiaSemanaAndAtivoTrue(diaSemana);
    }
}

