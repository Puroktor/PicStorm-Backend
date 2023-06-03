package ru.vsu.cs.picstorm.util;

import lombok.experimental.UtilityClass;
import org.springframework.lang.Nullable;
import ru.vsu.cs.picstorm.entity.ReactionType;

import static ru.vsu.cs.picstorm.entity.ReactionType.DISLIKE;
import static ru.vsu.cs.picstorm.entity.ReactionType.LIKE;

@UtilityClass
public class PublicationRatingUtils {

    public long calculateRatingChange(@Nullable ReactionType oldReaction, @Nullable ReactionType newReaction) {
        if (oldReaction == null && newReaction == null) {
            return 0;
        } else if (oldReaction == null) {
            return newReaction == LIKE ? 1 : -1;
        } else if (newReaction == null) {
            return oldReaction == LIKE ? -1 : 1;
        } else {
            return oldReaction.equals(DISLIKE) ? 2 : -2;
        }
    }
}
