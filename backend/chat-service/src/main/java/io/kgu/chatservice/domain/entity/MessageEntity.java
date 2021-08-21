package io.kgu.chatservice.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;
import javax.validation.constraints.Size;
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

    @Column(nullable = false, updatable = false)
    @Enumerated(value = EnumType.STRING)
    private MessageContentType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chats_session_id", nullable = false, updatable = false)
    private ChatEntity chat;

    @Column(nullable = false, updatable = false)
    private String fromUser;

    @Size(min = 1, max = 255)
    @Column(nullable = false, updatable = false)
    private String content;

    @Column(updatable = false)
    @ColumnDefault(value = "CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

}