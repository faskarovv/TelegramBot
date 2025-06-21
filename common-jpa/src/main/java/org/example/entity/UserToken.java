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
public class UserToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;

    private Instant expiresAt;

    private boolean used;

    @OneToOne
    private AppUser appUser;
}
