package ru.vsu.cs.picstorm.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.List;

@Entity
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Publication extends EntityWithId {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;
    @OneToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "picture_id", nullable = false)
    private Picture picture;
    @NotNull(message = "Enter publication type")
    private PublicationState state;
    /**
     *  Calculated value, added due to performance reasons
     */
    @NotNull(message = "Enter publication rating")
    @Builder.Default
    private Long rating = 0L;
    @OneToMany(mappedBy = "publication", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<Reaction> reactions;
    @CreationTimestamp
    private Instant created;
}
