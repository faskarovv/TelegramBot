package org.example.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AppFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String telegramFileId;
    private String s3Key;
    private String mimeType;
    private String fileName;
    private Long fileSize;

    @Column(nullable = false)
    private Instant uploadedAt;

    @ManyToOne
    @JoinColumn(name = "app_user_id")
    @JsonBackReference
    private AppUser appUser;
}
