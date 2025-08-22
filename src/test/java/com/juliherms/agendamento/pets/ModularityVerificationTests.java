package com.juliherms.agendamento.pets;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

import static org.assertj.core.api.Assertions.assertThatNoException;

class ModularityVerificationTests {

    @Test
    void modulesVerify() {
        var modules = ApplicationModules.of(PetsApplication.class);
        assertThatNoException().isThrownBy(modules::verify);
    }
}


