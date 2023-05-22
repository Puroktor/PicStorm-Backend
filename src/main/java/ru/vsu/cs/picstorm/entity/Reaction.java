package ru.vsu.cs.picstorm.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(name = "UniqueUserReaction", columnNames = { "publication_id", "user_id" })})
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Reaction extends EntityWithId {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull(message = "Enter reaction type")
    private ReactionType type;
    @ManyToOne
    @JoinColumn(name = "publication_id", nullable = false)
    private Publication publication;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @CreationTimestamp
    private Instant created;
}
