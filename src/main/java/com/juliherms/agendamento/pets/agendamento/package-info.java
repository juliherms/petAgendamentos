@org.springframework.modulith.ApplicationModule(
    allowedDependencies = {
        "users::api",
        "pets::api", 
        "services::api"
    }
)
package com.juliherms.agendamento.pets.agendamento;
