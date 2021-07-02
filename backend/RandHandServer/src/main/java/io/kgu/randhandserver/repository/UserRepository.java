package io.kgu.randhandserver.repository;

import io.kgu.randhandserver.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> getOneByAuthAndEmail(String auth, String email);

}
