package io.kgu.chatservice.domain.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "chats")
public class ChatEntity {

    @Id @Column(length = 36)
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    private String sessionId;

    @ElementCollection(fetch = FetchType.LAZY)
    @JoinTable(name = "chats_users",
            joinColumns = {@JoinColumn(name = "chat_id", updatable = false)})
    private List<String> userIds;

    @Column(nullable = false, updatable = false)
    private LocalDateTime syncTime;

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<MessageEntity> messages;

}
