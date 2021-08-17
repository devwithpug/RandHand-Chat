package io.kgu.chatservice.domain.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@Entity
@Table(name = "chats")
public class ChatEntity {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(columnDefinition = "BINARY(36)")
    private String sessionId;

    @ElementCollection(fetch = FetchType.LAZY)
    @JoinTable(name = "chats_users",
            joinColumns = {@JoinColumn(name = "chat_id")})
    private Set<String> userIds;

    @Column(nullable = false)
    private LocalDateTime syncTime;

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL)
    private List<MessageEntity> messages;

}
