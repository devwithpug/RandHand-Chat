package io.kgu.randhandserver.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class UserInfo implements Serializable {

    @Id
    @GeneratedValue
    @Column(name = "info_id")
    private Long id;

    @Column
    private String statusMessage;

    @Column
    private String picture;

    @LastModifiedDate
    private LocalDate lastModifiedAt;

    public static UserInfo create(User user) {

        return UserInfo.builder()
                .statusMessage("")
                .picture(user.getPicture())
                .build();

    }

}
