package io.kgu.userservice.repository;

import io.kgu.userservice.domain.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    boolean existsByUserId(String userId);
    boolean existsByEmail(String email);


    UserEntity findByUserId(String userId);
    UserEntity findByEmail(String email);

    void deleteByUserId(String userId);
}
