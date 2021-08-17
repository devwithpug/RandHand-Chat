package io.kgu.chatservice.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@Table(name = "messages")
@AllArgsConstructor
@NoArgsConstructor
public class MessageEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private MessageContentType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chats_session_id")
    private ChatEntity chat;

    @Column(nullable = false)
    private String fromUser;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false, updatable = false, insertable = false)
    @ColumnDefault(value = "CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

}