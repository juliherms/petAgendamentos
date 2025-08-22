package com.juliherms.agendamento.pets.users.internal.repo;

import com.juliherms.agendamento.pets.users.internal.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailIgnoreCase(String email);
}


