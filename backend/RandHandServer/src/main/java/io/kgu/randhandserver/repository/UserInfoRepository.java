package io.kgu.randhandserver.repository;

import io.kgu.randhandserver.domain.entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserInfoRepository extends JpaRepository<UserInfo, Long> {
}
