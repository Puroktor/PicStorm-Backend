package ru.vsu.cs.picstorm.dto.request;

import org.springframework.data.domain.Sort;

public enum SortConstraint {
    NONE,
    LIKED_FIRST,
    DISLIKED_FIRST;

    public Sort toDataSort() {
        return switch (this) {
            case NONE -> Sort.unsorted();
            case LIKED_FIRST -> Sort.by("rating").descending();
            case DISLIKED_FIRST -> Sort.by("rating").ascending();
        };
    }
}
