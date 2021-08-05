package io.kgu.chatservice.domain.entity;

import io.kgu.chatservice.domain.dto.UserDto;
import javassist.NotFoundException;
import javassist.tools.web.BadHttpRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.dao.DuplicateKeyException;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity implements Serializable {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, unique = true)
    private String userId;

    @Column(nullable = false, updatable = false)
    private String auth;

    @Column(nullable = false, length = 50, updatable = false)
    private String email;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 50)
    private String statusMessage;

    @Column
    private String picture;

    @ElementCollection
    @JoinTable(name = "users_friends",
            joinColumns = {@JoinColumn(name = "my_id")})
    private List<String> userFriends;

    @ElementCollection
    @JoinTable(name = "users_blocked",
            joinColumns = {@JoinColumn(name = "my_id")})
    private List<String> userBlocked;

    @Column(nullable = false, updatable = false, insertable = false)
    @ColumnDefault(value = "CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    public void update(UserDto userDto) {
        this.name = userDto.getName();
        this.statusMessage = userDto.getStatusMessage();
        this.picture = userDto.getPicture();
    }

    public void addFriend(String friendId) {

        if (this.userFriends.contains(friendId)) throw new DuplicateKeyException("Already exists friends");

        this.userFriends.add(friendId);
    }

    public void removeFriend(String friendId) {

        if (!this.userFriends.contains(friendId)) throw new EntityNotFoundException("friend does not exists");

        this.userFriends.remove(friendId);
    }

    public void blockUser(String blockId) {

        if (this.userBlocked.contains(blockId)) throw new DuplicateKeyException("Already blocked user");

        this.userBlocked.add(blockId);
    }

    public void unblockUser(String blockId) {

        if (!this.userBlocked.contains(blockId)) throw new EntityNotFoundException("blocked user does not exists");

        this.userBlocked.remove(blockId);
    }

    // TODO - ROLE(enum) 추가
}
