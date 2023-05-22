package ru.vsu.cs.picstorm.entity;

import org.springframework.lang.Nullable;

public enum ReactionType {
    LIKE,
    DISLIKE;

    public long calculateRatingChange(@Nullable ReactionType oldReaction) {
        if (oldReaction == null) {
            return this == LIKE ? 1 : -1;
        } else if (oldReaction.equals(this)) {
            return 0;
        } else {
            return oldReaction.equals(DISLIKE) ? 2 : -2;
        }
    }
}
