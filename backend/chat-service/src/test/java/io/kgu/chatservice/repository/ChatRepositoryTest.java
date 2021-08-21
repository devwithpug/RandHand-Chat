package io.kgu.chatservice.repository;

import io.kgu.chatservice.domain.dto.ChatDto;
import io.kgu.chatservice.domain.entity.ChatEntity;
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
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class ChatRepositoryTest {

    @Autowired
    private EntityManager em;
    @Autowired
    private ChatRepository chatRepository;
    private final ModelMapper mapper = new ModelMapper();

    private ChatDto createChatDto(String user1Id, String user2Id) {
        ChatDto chatDto = new ChatDto();
        chatDto.setUserIds(List.of(user1Id, user2Id));
        chatDto.setSyncTime(LocalDateTime.now());
        return chatDto;
    }

    @BeforeAll
    public void beforeAll() {

        // Set ModelMapper Matching Strategy to STRICT
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
    }

    /**
     * CREATE
     */

    @Test
    @DisplayName("ChatEntity 엔티티 객체 insert 문 수행")
    void create_ChatEntity() {
        // given
        ChatDto chatDto = createChatDto("user1Id", "user2Id");
        // when
        ChatEntity result = chatRepository.saveAndFlush(mapper.map(chatDto, ChatEntity.class));
        // then
        assertThat(result.getSessionId()).isNotNull().hasSize(36);
    }

    @Test
    @DisplayName("ChatEntity 의 syncTime 값이 없으면 저장이 불가능하다.")
    void create_ChatEntity_with_no_syncTime_exception() {
        // given
        ChatDto chatDto = createChatDto("user1Id", "user2Id");
        // when
        chatDto.setSyncTime(null);
        // then
        assertThatThrownBy(() -> chatRepository.saveAndFlush(mapper.map(chatDto, ChatEntity.class)))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("ConstraintViolationException");
    }

    /**
     * READ
     */

    @Test
    @DisplayName("UNIQUE 한 sessionId 값으로 ChatEntity 를 조회할 수 있다.")
    void select_ChatEntity_with_findChatEntityBySessionId() {
        // given
        ChatDto chatDto = createChatDto("user1Id", "user2Id");
        ChatEntity chatRoom = chatRepository.saveAndFlush(mapper.map(chatDto, ChatEntity.class));
        // when
        ChatEntity result = chatRepository.findChatEntityBySessionId(chatRoom.getSessionId());
        // then
        assertThat(result).isEqualTo(chatRoom);
    }

    /**
     * UPADTE
     */

    @Test
    @DisplayName("ChatEntity 의 userIds 값은 변경이 불가능하다.")
    void not_updatable_ChatEntity_userIds() {
        // given
        ChatDto chatDto = createChatDto("user1Id", "user2Id");
        ChatEntity chatRoom = chatRepository.saveAndFlush(mapper.map(chatDto, ChatEntity.class));
        // when
        chatRoom.setUserIds(List.of("user3Id", "user4Id"));
        // then
        assertThatThrownBy(() -> chatRepository.saveAndFlush(chatRoom))
                .isInstanceOf(UnsupportedOperationException.class);

    }

    /**
     * DELETE
     */

    @Test
    @DisplayName("UNIQUE 한 sessionId 값으로 ChatEntity 를 삭제할 수 있다.")
    void delete_ChatEntity_with_deleteBySessionId() {
        // given
        ChatDto chatDto = createChatDto("user1Id", "user2Id");
        ChatEntity chatRoom = chatRepository.saveAndFlush(mapper.map(chatDto, ChatEntity.class));
        // when
        chatRepository.deleteBySessionId(chatRoom.getSessionId());
        // then
        ChatEntity result = chatRepository.findChatEntityBySessionId(chatRoom.getSessionId());
        assertThat(result).isNull();
    }

}