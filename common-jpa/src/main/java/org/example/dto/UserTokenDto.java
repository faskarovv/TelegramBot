package org.example.dto;

import lombok.*;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserTokenDto {
    private String token;
    private Instant expiresAt;
    private boolean used;
    private Long appUserId;
}
