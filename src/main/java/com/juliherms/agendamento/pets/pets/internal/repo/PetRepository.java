package com.juliherms.agendamento.pets.pets.internal.repo;

import com.juliherms.agendamento.pets.pets.internal.domain.Pet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PetRepository extends JpaRepository<Pet, Long> {}


