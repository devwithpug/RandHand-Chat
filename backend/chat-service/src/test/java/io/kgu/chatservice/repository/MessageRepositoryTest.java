package io.kgu.chatservice.repository;

import io.kgu.chatservice.domain.dto.MessageDto;
import io.kgu.chatservice.domain.entity.ChatEntity;
import io.kgu.chatservice.domain.entity.MessageContentType;
import io.kgu.chatservice.domain.entity.MessageEntity;
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
import javax.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.kgu.chatservice.domain.entity.MessageContentType.IMAGE;
import static io.kgu.chatservice.domain.entity.MessageContentType.TEXT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class MessageRepositoryTest {

    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private ChatRepository chatRepository;
    @Autowired
    private EntityManager em;
    private final ModelMapper mapper = new ModelMapper();

    private ChatEntity chatRoom1_2;
    private ChatEntity chatRoom2_3;
    private ChatEntity chatRoom1_3;

    @BeforeAll
    public void beforeAll() {
        // Set ModelMapper Matching Strategy to STRICT
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        // init ChatRoom Entities
        chatRoom1_2 = new ChatEntity();
        chatRoom1_2.setUserIds(List.of("user1Id", "user2Id"));
        chatRoom1_2.setMessages(new ArrayList<>());
        chatRoom1_2.setSyncTime(LocalDateTime.now());

        chatRoom2_3 = new ChatEntity();
        chatRoom2_3.setUserIds(List.of("user2Id", "user3Id"));
        chatRoom2_3.setMessages(new ArrayList<>());
        chatRoom2_3.setSyncTime(LocalDateTime.now());

        chatRoom1_3 = new ChatEntity();
        chatRoom1_3.setUserIds(List.of("user1Id", "user3Id"));
        chatRoom1_3.setMessages(new ArrayList<>());
        chatRoom1_3.setSyncTime(LocalDateTime.now());

        chatRepository.saveAllAndFlush(List.of(chatRoom1_2, chatRoom2_3, chatRoom1_3));
    }

    private MessageDto createMessageDto(MessageContentType type, String from, String content) {
        MessageDto messageDto = new MessageDto();
        messageDto.setType(type);
        messageDto.setFromUser(from);
        messageDto.setContent(content);

        return messageDto;
    }

    /**
     * CREATE
     */

    @Test
    @DisplayName("MessageEntity 엔티티 객체 insert")
    void create_MessageEntity() {
        // given
        MessageDto messageDto = createMessageDto(TEXT, "user1Id", "user1->user2");
        MessageEntity message = mapper.map(messageDto, MessageEntity.class);
        // when
        message.setChat(chatRoom1_2);
        MessageEntity result = messageRepository.saveAndFlush(message);
        // then
        assertThat(result).isNotNull().isEqualTo(message);
        assertThat(result.getChat()).isEqualTo(chatRoom1_2);
    }

    @Test
    @DisplayName("type, fromUser, content, chat 값이 없는 경우 MessageEntity 값 저장이 불가능하다.")
    void create_MessageEntity_with_no_values_exception() {
        // given
        MessageDto messageDto = createMessageDto(TEXT, "user1Id", "message");
        MessageEntity messageWithNoType = mapper.map(messageDto, MessageEntity.class);
        messageWithNoType.setChat(chatRoom1_2);
        MessageEntity messageWithNoFromUser = mapper.map(messageDto, MessageEntity.class);
        messageWithNoFromUser.setChat(chatRoom1_2);
        MessageEntity messageWithNoContent = mapper.map(messageDto, MessageEntity.class);
        messageWithNoContent.setChat(chatRoom1_2);
        MessageEntity messageWithNoChat = mapper.map(messageDto, MessageEntity.class);
        messageWithNoChat.setChat(chatRoom1_2);
        // when
        messageWithNoType.setType(null);
        messageWithNoFromUser.setFromUser(null);
        messageWithNoContent.setContent(null);
        messageWithNoChat.setChat(null);
        // then
        assertThatThrownBy(() -> messageRepository.saveAndFlush(messageWithNoType))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> messageRepository.saveAndFlush(messageWithNoFromUser))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> messageRepository.saveAndFlush(messageWithNoContent))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> messageRepository.saveAndFlush(messageWithNoChat))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("content 값이 비어있는 경우 MessageEntity 값 저장이 불가능하다.")
    void create_MessageEntity_with_empty_content_exception() {
        // given
        MessageDto messageDto = createMessageDto(TEXT, "user1Id", "message");
        MessageEntity messageWithEmptyContent = mapper.map(messageDto, MessageEntity.class);
        messageWithEmptyContent.setChat(chatRoom1_2);
        // when
        messageWithEmptyContent.setContent("");
        // then
        assertThatThrownBy(() -> messageRepository.saveAndFlush(messageWithEmptyContent))
                .isInstanceOf(ConstraintViolationException.class);
    }

    /**
     * READ
     */

    @Test
    @DisplayName("ChatEntity 의 모든 MessageEntity 들을 조회할 수 있다.")
    void select_MessageEntity_with_findAllByChat() {
        // given
        MessageDto messageDto1 = createMessageDto(TEXT, "user1Id", "user1->user2");
        MessageEntity message1 = mapper.map(messageDto1, MessageEntity.class);
        MessageDto messageDto2 = createMessageDto(TEXT, "user2Id", "user2->user1");
        MessageEntity message2 = mapper.map(messageDto2, MessageEntity.class);
        MessageDto messageDto3 = createMessageDto(TEXT, "user3Id", "user3->user1");
        MessageEntity message3 = mapper.map(messageDto3, MessageEntity.class);

        message1.setChat(chatRoom1_2);
        message2.setChat(chatRoom1_2);
        message3.setChat(chatRoom1_3);
        messageRepository.saveAllAndFlush(List.of(message1, message2, message3));
        // when
        List<MessageEntity> result = messageRepository.findAllByChat(chatRoom1_2);
        // then
        assertThat(result).hasSize(2).containsExactly(message1, message2);
    }

    @Test
    @DisplayName("ChatEntity 에서 임의의 LocalDateTime 이후의 메세지들을 조회할 수 있다.")
    void select_MessageEntity_with_findAllByChatAndCreatedAtAfter() {
        // given
        MessageDto messageDto1 = createMessageDto(TEXT, "user1Id", "user1->user2 12:00");
        MessageEntity message1 = mapper.map(messageDto1, MessageEntity.class);
        message1.setCreatedAt(LocalDateTime.parse("2021-08-21T12:00:00"));
        MessageDto messageDto2 = createMessageDto(TEXT, "user1Id", "user1->user2 12:30");
        MessageEntity message2 = mapper.map(messageDto2, MessageEntity.class);
        message2.setCreatedAt(LocalDateTime.parse("2021-08-21T12:30:00"));
        MessageDto messageDto3 = createMessageDto(TEXT, "user2Id", "user2->user1 13:00");
        MessageEntity message3 = mapper.map(messageDto3, MessageEntity.class);
        message3.setCreatedAt(LocalDateTime.parse("2021-08-21T13:00:00"));
        MessageDto messageDto4 = createMessageDto(TEXT, "user3Id", "user3->user2 13:01");
        MessageEntity message4 = mapper.map(messageDto4, MessageEntity.class);
        message4.setCreatedAt(LocalDateTime.parse("2021-08-21T13:01:00"));

        message1.setChat(chatRoom1_2);
        message2.setChat(chatRoom1_2);
        message3.setChat(chatRoom1_2);
        message4.setChat(chatRoom2_3);
        messageRepository.saveAllAndFlush(List.of(message1, message2, message3, message4));
        // when
        List<MessageEntity> result = messageRepository.findAllByChatAndCreatedAtAfter(chatRoom1_2, LocalDateTime.parse("2021-08-21T12:29:59"));
        // then
        assertThat(result).hasSize(2).containsExactly(message2, message3);
    }

    /**
     * UPDATE
     */

    @Test
    @DisplayName("MessageEntity 의 값들은 변경이 불가능하다.")
    void not_updatable_MessageEntity_columns() {
        // given
        MessageDto messageDto = createMessageDto(TEXT, "user1Id", "user1->user2");
        MessageEntity message = mapper.map(messageDto, MessageEntity.class);
        message.setChat(chatRoom1_2);
        messageRepository.saveAndFlush(message);
        // when
        message.setType(IMAGE);
        message.setContent("CHANGED_CONTENT");
        message.setCreatedAt(LocalDateTime.parse("2021-08-12T12:00:00"));
        message.setFromUser("user2Id");
        message.setChat(chatRoom2_3);
        messageRepository.saveAndFlush(message);
        // then
        em.clear();
        MessageEntity result = messageRepository.findById(message.getId()).get();
        assertThat(result.getType()).isNotEqualTo(IMAGE);
        assertThat(result.getContent()).isNotEqualTo("CHANGED_CONTENT");
        assertThat(result.getCreatedAt()).isNotEqualTo(LocalDateTime.parse("2021-08-12T12:00:00"));
        assertThat(result.getFromUser()).isNotEqualTo("user2Id");
        assertThat(result.getChat()).isNotEqualTo(chatRoom2_3);
    }

    /**
     * DELETE
     */

    @Test
    @DisplayName("MessageEntity delete 쿼리 테스트")
    void delete_MessageEntity() {
        // given
        MessageDto messageDto = createMessageDto(TEXT, "user1Id", "user1->user2");
        MessageEntity message = mapper.map(messageDto, MessageEntity.class);
        message.setChat(chatRoom1_2);
        messageRepository.saveAndFlush(message);
        // when
        messageRepository.delete(message);
        // then
        Optional<MessageEntity> result = messageRepository.findById(message.getId());
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("ChatEntity 를 삭제하면 속해있는 MessageEntity 모두 삭제된다.")
    void delete_all_MessageEntity_by_deleting_ChatEntity() {
        // given
        MessageDto messageDto1 = createMessageDto(TEXT, "user1Id", "user1->user2");
        MessageEntity message1 = mapper.map(messageDto1, MessageEntity.class);
        MessageDto messageDto2 = createMessageDto(TEXT, "user1Id", "user1->user2");
        MessageEntity message2 = mapper.map(messageDto2, MessageEntity.class);

        message1.setChat(chatRoom1_2);
        message2.setChat(chatRoom1_2);
        messageRepository.saveAllAndFlush(List.of(message1, message2));
        // when
        chatRepository.delete(chatRoom1_2);
        em.flush();
        em.clear();
        // then
        Optional<MessageEntity> result1 = messageRepository.findById(message1.getId());
        Optional<MessageEntity> result2 = messageRepository.findById(message2.getId());

        assertThat(result1).isEmpty();
        assertThat(result2).isEmpty();
    }
}