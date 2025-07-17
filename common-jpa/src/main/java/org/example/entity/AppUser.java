package org.example.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;
import org.example.entity.enums.UserState;
import org.hibernate.annotations.CreationTimestamp;


import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = "id")
@Table(name = "app_user",
    indexes = {
        @Index(name = "idx_email" , columnList = "email")
    }
)
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long telegramBotId;

    @CreationTimestamp
    private LocalDateTime localDateTime;

    private String firstName;
    private String lastName;
    private String username;

    @Email(message = "email is not valid")
    private String email;
    private Boolean isActive;

    @Enumerated(EnumType.STRING)
    private UserState userState;

//    @OneToOne(mappedBy = "appUser")
//    private UserToken userToken;

    @OneToMany(mappedBy = "appUser")
    private List<AppFile> appFileList;
}
