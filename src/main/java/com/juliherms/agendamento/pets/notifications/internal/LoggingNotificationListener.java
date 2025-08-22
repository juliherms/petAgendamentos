package com.juliherms.agendamento.pets.notifications.internal;

import com.juliherms.agendamento.pets.users.api.UserCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
class LoggingNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(LoggingNotificationListener.class);

    @EventListener
    void on(UserCreatedEvent event) {
        log.info("[NOTIFY] userId={} channel={} token={} expiresAt={} email={} phone={}",
                event.userId(),
                event.canalPreferido(),
                event.tokenVerificacao(),
                event.expiresAt(),
                event.email(),
                event.telefone());
    }
}


