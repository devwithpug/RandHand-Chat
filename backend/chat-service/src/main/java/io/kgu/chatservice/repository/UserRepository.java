package io.kgu.chatservice.repository;

import io.kgu.chatservice.domain.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    boolean existsByUserId(String userId);
    boolean existsByAuthAndEmail(String auth, String email);

    UserEntity findByAuthAndEmail(String auth, String email);
    UserEntity findByUserId(String userId);
    UserEntity findByEmail(String email);

    @Query("select u from UserEntity u where u.userId in ?1")
    List<UserEntity> findByUserId(List<String> userIds);

    void deleteByUserId(String userId);
}
