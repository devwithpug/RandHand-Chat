package io.kgu.randhandserver.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"userFriends", "userBlocked"})
@EntityListeners(AuditingEntityListener.class)
public class User implements Serializable {

    @Id
    @GeneratedValue
    @JsonIgnore
    private Long id;

    @Column
    private String auth;

    @Column
    private String email;

    @Column
    private String name;

    @Column
    private String picture;

    @OneToOne
    @JoinColumn(name = "info_id")
    private UserInfo userInfo;

    @ElementCollection
    @JoinTable(name = "user_friends",
            joinColumns = {@JoinColumn(name = "my_id")})
    private List<Long> userFriends;

    @ElementCollection
    @JoinTable(name = "user_blocked",
            joinColumns = {@JoinColumn(name = "my_id")})
    private List<Long> userBlocked;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @CreatedDate
    private LocalDate registeredAt;

}
