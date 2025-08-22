package com.juliherms.agendamento.pets.verificador.internal.service;

import com.juliherms.agendamento.pets.verificador.internal.exception.VerificadorExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Serviço para envio de SMS de verificação.
 * Simula o envio real de SMS com logging detalhado.
 */
@Service
public class SMSService {

    private static final Logger log = LoggerFactory.getLogger(SMSService.class);

    /**
     * Simula o envio de um SMS de verificação.
     * 
     * @param telefone Número de telefone do destinatário
     * @param token Token de verificação
     * @param expiresAt Data de expiração do token
     * @return true se o envio foi simulado com sucesso
     * @throws VerificadorExceptionHandler.FalhaEnvioSMSException se houver falha no envio
     */
    public boolean enviarSMSVerificacao(String telefone, String token, Instant expiresAt) {
        try {
            log.info("[SMS] Iniciando envio de SMS de verificação para: {}", telefone);
            
            // Simula tempo de processamento
            simularProcessamento();
            
            // Simula falha ocasional (3% de chance)
            if (ThreadLocalRandom.current().nextDouble() < 0.03) {
                throw new VerificadorExceptionHandler.FalhaEnvioSMSException(
                    "Falha simulada no envio de SMS para: " + telefone
                );
            }
            
            // Simula o envio do SMS
            String mensagem = gerarMensagemSMS(token, expiresAt);
            
            log.info("[SMS] SMS enviado com sucesso para: {}", telefone);
            log.debug("[SMS] Mensagem: {}", mensagem);
            
            return true;
            
        } catch (Exception e) {
            log.error("[SMS] Erro ao enviar SMS para {}: {}", telefone, e.getMessage());
            throw new VerificadorExceptionHandler.FalhaEnvioSMSException(
                "Falha no envio de SMS: " + e.getMessage()
            );
        }
    }
    
    private String gerarMensagemSMS(String token, Instant expiresAt) {
        long horasRestantes = Duration.between(Instant.now(), expiresAt).toHours();

        //TODO: parametrizar mensagem SMS
        return String.format(
            "Pets API: Seu codigo de verificacao e %s. Expira em %d horas. " +
            "Nao compartilhe este codigo.", 
            token, horasRestantes
        );
    }
    
    private void simularProcessamento() {
        try {
            // Simula tempo de processamento entre 50ms e 200ms (SMS é mais rápido)
            Thread.sleep(ThreadLocalRandom.current().nextLong(50, 200));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
