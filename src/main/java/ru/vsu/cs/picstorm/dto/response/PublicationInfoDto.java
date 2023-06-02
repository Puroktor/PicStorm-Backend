package ru.vsu.cs.picstorm.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.vsu.cs.picstorm.entity.ReactionType;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicationInfoDto {
    private Long publicationId;
    private Long ownerId;
    private String ownerNickname;
    private Long rating;
    private Instant uploaded;
    private ReactionType userReaction;
}
