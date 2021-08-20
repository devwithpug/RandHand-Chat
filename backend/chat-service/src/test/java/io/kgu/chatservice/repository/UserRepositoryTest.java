package io.kgu.chatservice.repository;

import io.kgu.chatservice.domain.dto.UserDto;
import io.kgu.chatservice.domain.entity.UserEntity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class UserRepositoryTest {

    @Autowired
    private EntityManager em;
    @Autowired
    private UserRepository userRepository;
    private final ModelMapper mapper = new ModelMapper();

    /**
     * CREATE
     */

    private UserDto createUserDto(String name, String auth, String email) {
        UserDto userDto = new UserDto();
        userDto.setName(name);
        userDto.setUserId(UUID.randomUUID().toString());
        userDto.setAuth(auth);
        userDto.setPicture("https://test.picture/dir");
        userDto.setStatusMessage("");
        userDto.setEmail(email);

        return userDto;
    }

    @BeforeAll
    public void beforeAll() {

        // Set ModelMapper Matching Strategy to STRICT
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
    }

    @Test
    @DisplayName("UserEntity 엔티티 객체 insert 문 수행")
    void create_UserEntity() {
        // given
        UserDto userDtoA = createUserDto("userA", "google", "userA@email.com");
        // when
        UserEntity result = userRepository.save(mapper.map(userDtoA, UserEntity.class));
        // then
        assertThat(result.getUserId()).isEqualTo(userDtoA.getUserId());
    }

    @Test
    @DisplayName("UserEntity 의 userId 값이 없으면 엔티티 저장이 불가능하다.")
    void create_UserEntity_with_no_userId_exception() {
        // given
        UserDto userDtoA = createUserDto("userA", "google", "userA@email.com");
        // when
        userDtoA.setUserId(null);
        // then
        assertThatThrownBy(() -> userRepository.saveAndFlush(mapper.map(userDtoA, UserEntity.class)))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("ConstraintViolationException");
    }

    @Test
    @DisplayName("auth & email 값은 UNIQUE 하기 때문에 중복 가입이 불가능하다.")
    void create_UserEntity_with_duplicated_auth_and_email_exception() {
        // given
        UserDto userDtoA = createUserDto("UserA", "google", "userA@email.com");
        UserDto userWithDuplicatedEmail = createUserDto("UserWithDuplicatedEmail", "google", "userA@email.com");
        // when
        userRepository.saveAndFlush(mapper.map(userDtoA, UserEntity.class));
        // then
        assertThatThrownBy(() -> userRepository.saveAndFlush(mapper.map(userWithDuplicatedEmail, UserEntity.class)))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("ConstraintViolationException");
    }

    /**
     * READ
     */

    @Test
    @DisplayName("UNIQUE 한 userId 값으로 UserEntity exists 쿼리가 가능하다.")
    void exists_UserEntity_with_existsByUserId() {
        // given
        UserDto userDtoA = createUserDto("UserA", "google", "userA@email.com");
        UserEntity userA = userRepository.saveAndFlush(mapper.map(userDtoA, UserEntity.class));
        // when
        boolean resultWithUser = userRepository.existsByUserId(userA.getUserId());
        boolean resultWithNoUser = userRepository.existsByUserId("NoUser");
        // then
        assertThat(resultWithUser).isTrue();
        assertThat(resultWithNoUser).isFalse();
    }

    @Test
    @DisplayName("UNIQUE 한 userId 값으로 UserEntity 조회가 가능하다.")
    void select_UserEntity_with_findByUserId() {
        // given
        UserDto userDtoA = createUserDto("UserA", "google", "userA@email.com");
        UserEntity userA = userRepository.saveAndFlush(mapper.map(userDtoA, UserEntity.class));
        // when
        UserEntity resultWithUser = userRepository.findByUserId(userA.getUserId());
        UserEntity resultWithNoUser = userRepository.findByUserId("NoUser");
        // then
        assertThat(resultWithUser).isNotNull().isEqualTo(userA);
        assertThat(resultWithNoUser).isNull();
    }

    @Test
    @DisplayName("UNIQUE 한 auth & email 값으로 UserEntity exists 쿼리가 가능하다.")
    void exists_UserEntity_with_existsByAuthAndEmail() {
        // given
        UserDto userDtoA = createUserDto("UserA", "google", "userA@email.com");
        UserEntity userA = userRepository.saveAndFlush(mapper.map(userDtoA, UserEntity.class));
        // when
        boolean resultWithUser = userRepository.existsByAuthAndEmail(userA.getAuth(), userA.getEmail());
        boolean resultWithInvalidAuth = userRepository.existsByAuthAndEmail("INVALID AUTH", userA.getEmail());
        // then
        assertThat(resultWithUser).isTrue();
        assertThat(resultWithInvalidAuth).isFalse();
    }

    @Test
    @DisplayName("UNIQUE 한 auth & email 값으로 UserEntity 조회가 가능하다.")
    void select_UserEntity_with_findByAuthAndEmail() {
        // given
        UserDto userDtoA = createUserDto("UserA", "google", "userA@email.com");
        UserEntity userA = userRepository.saveAndFlush(mapper.map(userDtoA, UserEntity.class));
        // when
        UserEntity resultWithUser = userRepository.findByAuthAndEmail(userA.getAuth(), userA.getEmail());
        UserEntity resultWithInvalidAuth = userRepository.findByAuthAndEmail("INVALID AUTH", userA.getEmail());
        // then
        assertThat(resultWithUser).isNotNull().isEqualTo(userA);
        assertThat(resultWithInvalidAuth).isNull();
    }

    @Test
    @DisplayName("in 절을 통해 UserEntity 를 한꺼번에 조회가 가능하다.")
    void select_UserEntities_with_in_query_FriendsEntityByUserIds() {
        // given
        UserDto userDtoA = createUserDto("UserA", "google", "userA@email.com");
        UserEntity userA = userRepository.saveAndFlush(mapper.map(userDtoA, UserEntity.class));

        UserDto friendA = createUserDto("friendA", "auth", "friendA@email.com");
        UserDto friendB = createUserDto("friendB", "auth", "friendB@email.com");

        List<UserEntity> friends = userRepository.saveAllAndFlush(List.of(
                mapper.map(friendA, UserEntity.class),
                mapper.map(friendB, UserEntity.class)
        ));
        // when
        userA.setUserFriends(friends.stream()
                .map(UserEntity::getUserId)
                .collect(Collectors.toList()));
        userRepository.saveAndFlush(userA);
        // then
        List<UserEntity> result = userRepository.findAllByUserIds(userA.getUserFriends());
        assertThat(result).isEqualTo(friends);
    }

    /**
     * UPDATE
     */

    @Test
    @DisplayName("UserEntity 의 name, statusMessage 는 변경이 가능하다.")
    void update_UserEntity_of_name_and_statusMessage() {
        // given
        UserDto userDtoA = createUserDto("UserA", "google", "userA@email.com");
        UserEntity userA = userRepository.saveAndFlush(mapper.map(userDtoA, UserEntity.class));
        // when
        userA.setName("UserAA");
        userA.setStatusMessage("HELLO WORLD!");
        userRepository.saveAndFlush(userA);
        // then
        UserEntity result = userRepository.findByUserId(userA.getUserId());
        assertThat(result.getName()).isEqualTo("UserAA");
        assertThat(result.getStatusMessage()).isEqualTo("HELLO WORLD!");
    }

    @Test
    @DisplayName("UserEntity 의 userId, auth, email 은 변경이 불가능하다.")
    void update_UserEntity_of_not_updatable_columns() {
        // given
        UserDto userDtoA = createUserDto("UserA", "google", "userA@email.com");
        UserEntity userA = userRepository.saveAndFlush(mapper.map(userDtoA, UserEntity.class));

        String userAUserId = userA.getUserId();
        // when
        userA.setUserId("CHANGED_USER_ID");
        userA.setAuth("CHANGED_AUTH");
        userA.setEmail("changed@email.com");
        userRepository.saveAndFlush(userA);
        // then
        em.clear(); // clear in-memory session
        UserEntity result = userRepository.findByUserId(userAUserId);
        assertThat(result).isNotEqualTo(userA);
    }

    /**
     * DELETE
     */

    @Test
    @DisplayName("userId 값을 통해 UserEntity 삭제가 가능하다.")
    void delete_UserEntity_with_deleteByUserId() {
        // given
        UserDto userDtoA = createUserDto("UserA", "google", "userA@email.com");
        UserEntity userA = userRepository.saveAndFlush(mapper.map(userDtoA, UserEntity.class));
        // when
        userRepository.deleteByUserId(userA.getUserId());
        // then
        UserEntity result = userRepository.findByUserId(userA.getUserId());
        assertThat(result).isNull();
    }
}