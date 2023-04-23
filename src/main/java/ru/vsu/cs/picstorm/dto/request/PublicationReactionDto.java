package ru.vsu.cs.picstorm.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.vsu.cs.picstorm.entity.ReactionType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublicationReactionDto {
    @NotNull(message = "Предоставьте реакцию")
    private ReactionType reaction;
}
