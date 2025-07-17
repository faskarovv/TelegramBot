package org.example.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;


@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(exclude = "id")
@Table(name = "user_token",
        indexes = {
                @Index(name = "idx_app_user_id", columnList = "app_user_id"),
                @Index(name = "idx_token", columnList = "token")
        }
)
public class UserToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String token;

    private Instant expiresAt;

    private boolean used;

//    @OneToOne
//    @JoinColumn(name = "app_user_id" , referencedColumnName = "id")
//    private AppUser appUser;

    @Column(name = "app_user_id" , nullable = false , unique = true)
    private Long appUserId;
}
