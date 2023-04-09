package ru.vsu.cs.picstorm.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Subscription extends EntityWithId {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "subscriber_id", nullable = false)
    private User subscriber;
    @ManyToOne
    @JoinColumn(name = "target_id", nullable = false)
    private User target;
    @CreatedDate
    private Instant created;
}
