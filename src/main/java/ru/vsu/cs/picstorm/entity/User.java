package ru.vsu.cs.picstorm.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.lang.Nullable;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "app_user")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class User extends EntityWithId {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "avatar_id")
    @Nullable
    private Picture avatar;
    @NotBlank(message = "Enter user nickname")
    @Size(max = 50, message = "User nickname length must be <= 50 characters")
    @Column(unique = true)
    private String nickname;
    @NotBlank(message = "Enter your email")
    @Size(max = 320, message = "Email length must be <= 320 characters")
    @Email(message = "Not valid email", regexp = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
            + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$")
    @Column(unique = true)
    private String email;
    @NotBlank(message = "Enter user password hash")
    private String passwordHash;
    @NotNull(message = "Enter user role")
    private UserRole role;
    @OneToMany(mappedBy = "owner", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<Publication> publications;
    @CreationTimestamp
    private Instant created;
}
