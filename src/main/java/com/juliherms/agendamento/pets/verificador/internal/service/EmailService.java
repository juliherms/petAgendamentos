package com.juliherms.agendamento.pets.verificador.internal.service;

import com.juliherms.agendamento.pets.verificador.internal.exception.VerificadorExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Serviço para envio de e-mails de verificação.
 * Simula o envio real de e-mails com logging detalhado.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    /**
     * Simula o envio de um e-mail de verificação.
     * 
     * @param destinatario Endereço de e-mail do destinatário
     * @param token Token de verificação
     * @param expiresAt Data de expiração do token
     * @return true se o envio foi simulado com sucesso
     * @throws VerificadorExceptionHandler.FalhaEnvioEmailException se houver falha no envio
     */
    public boolean enviarEmailVerificacao(String destinatario, String token, Instant expiresAt) {
        try {
            log.info("[EMAIL] Iniciando envio de e-mail de verificação para: {}", destinatario);
            
            // Simula tempo de processamento
            simularProcessamento();
            
            // Simula falha ocasional (5% de chance)
            if (ThreadLocalRandom.current().nextDouble() < 0.05) {
                throw new VerificadorExceptionHandler.FalhaEnvioEmailException(
                    "Falha simulada no envio de e-mail para: " + destinatario
                );
            }
            
            // Simula o envio do e-mail
            String assunto = "Verificação de Conta - Pets API";
            String corpo = gerarCorpoEmail(token, expiresAt);
            
            log.info("[EMAIL] E-mail enviado com sucesso para: {}", destinatario);
            log.debug("[EMAIL] Assunto: {}", assunto);
            log.debug("[EMAIL] Corpo: {}", corpo);
            
            return true;
            
        } catch (Exception e) {
            log.error("[EMAIL] Erro ao enviar e-mail para {}: {}", destinatario, e.getMessage());
            throw new VerificadorExceptionHandler.FalhaEnvioEmailException(
                "Falha no envio de e-mail: " + e.getMessage()
            );
        }
    }
    
    private String gerarCorpoEmail(String token, Instant expiresAt) {
        long horasRestantes = Duration.between(Instant.now(), expiresAt).toHours();

        //TODO: parametrizsr este template de e-mail
        return String.format("""
            Olá!
            
            Você solicitou a verificação da sua conta na Pets API.
            
            Seu código de verificação é: %s
            
            Este código expira em %d horas.
            
            Se você não solicitou esta verificação, ignore este e-mail.
            
            Atenciosamente,
            Equipe Pets API
            """, token, horasRestantes);
    }
    
    private void simularProcessamento() {
        try {
            // Simula tempo de processamento entre 100ms e 500ms
            Thread.sleep(ThreadLocalRandom.current().nextLong(100, 500));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
