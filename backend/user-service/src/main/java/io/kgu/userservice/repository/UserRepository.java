package io.kgu.userservice.repository;

import io.kgu.userservice.domain.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    boolean existsByUserId(String userId);
    UserEntity findByUserId(String userId);
    void deleteByUserId(String userId);
}
