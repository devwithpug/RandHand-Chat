package io.kgu.chatservice.domain.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Data
@Entity
@Table(name = "chats")
public class ChatEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, unique = true)
    private String sessionId;

    @ElementCollection
    @JoinTable(name = "chats_users",
            joinColumns = {@JoinColumn(name = "chat_id")})
    private List<String> userIds;

}
