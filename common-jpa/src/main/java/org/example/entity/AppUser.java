package org.example.entity;


import jakarta.persistence.*;
import lombok.*;
import org.example.entity.enums.UserState;
import org.hibernate.annotations.CreationTimestamp;


import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = "id")
@Table(name = "app_user")
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
    private String email;
    private Boolean isActive;

    @Enumerated(EnumType.STRING)
    private UserState userState;

}
