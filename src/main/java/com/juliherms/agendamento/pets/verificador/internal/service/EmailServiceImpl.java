package com.juliherms.agendamento.pets.verificador.internal.service;

import com.juliherms.agendamento.pets.verificador.internal.exception.VerificadorExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Implementação simulada do serviço de envio de e-mails.
 * Simula o envio real de e-mails com logging detalhado.
 */
@Service
@Qualifier("emailServiceSimulado")
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Override
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

    @Override
    public void enviarEmailTexto(String from, String to, String subject, String body) {
        try {
            log.info("[EMAIL] Simulando envio de e-mail de texto para: {}", to);
            simularProcessamento();
            log.info("[EMAIL] E-mail de texto enviado com sucesso para: {}", to);
        } catch (Exception e) {
            log.error("[EMAIL] Erro ao enviar e-mail de texto para {}: {}", to, e.getMessage());
            throw new VerificadorExceptionHandler.FalhaEnvioEmailException(
                "Falha no envio de e-mail de texto: " + e.getMessage()
            );
        }
    }

    @Override
    public void enviarEmailHtml(String from, String to, String subject, String html, Map<String, byte[]> anexos) throws Exception {
        try {
            log.info("[EMAIL] Simulando envio de e-mail HTML para: {}", to);
            simularProcessamento();
            
            if (anexos != null && !anexos.isEmpty()) {
                log.info("[EMAIL] E-mail HTML com {} anexo(s) enviado com sucesso para: {}", anexos.size(), to);
            } else {
                log.info("[EMAIL] E-mail HTML enviado com sucesso para: {}", to);
            }
        } catch (Exception e) {
            log.error("[EMAIL] Erro ao enviar e-mail HTML para {}: {}", to, e.getMessage());
            throw new VerificadorExceptionHandler.FalhaEnvioEmailException(
                "Falha no envio de e-mail HTML: " + e.getMessage()
            );
        }
    }
    
    private String gerarCorpoEmail(String token, Instant expiresAt) {
        long horasRestantes = Duration.between(Instant.now(), expiresAt).toHours();

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
