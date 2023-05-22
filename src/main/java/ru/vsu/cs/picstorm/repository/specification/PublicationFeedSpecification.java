package ru.vsu.cs.picstorm.repository.specification;

import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;
import ru.vsu.cs.picstorm.dto.request.DateConstraint;
import ru.vsu.cs.picstorm.dto.request.UserConstraint;
import ru.vsu.cs.picstorm.entity.Publication;
import ru.vsu.cs.picstorm.entity.PublicationState;
import ru.vsu.cs.picstorm.entity.Subscription;
import ru.vsu.cs.picstorm.entity.User;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class PublicationFeedSpecification implements Specification<Publication> {
    private final DateConstraint dateConstraint;
    private final UserConstraint userConstraint;
    private final User viewer;
    private final User filterUser;

    @Nullable
    @Override
    public Predicate toPredicate(Root<Publication> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();
        Path<Instant> createdPath = root.get("created");
        if (dateConstraint.equals(DateConstraint.DAY)) {
            Predicate predicate = criteriaBuilder.greaterThanOrEqualTo(createdPath, Instant.now().minus(1, ChronoUnit.DAYS));
            predicates.add(predicate);
        } else if (dateConstraint.equals(DateConstraint.WEEK)) {
            Predicate predicate = criteriaBuilder.greaterThanOrEqualTo(createdPath, Instant.now().minus(7, ChronoUnit.DAYS));
            predicates.add(predicate);
        }
        if (userConstraint.equals(UserConstraint.SPECIFIED)) {
            Predicate predicate = criteriaBuilder.and(root.get("owner").in(filterUser));
            predicates.add(predicate);
        } else if (userConstraint.equals(UserConstraint.SUBSCRIPTIONS)) {
            Subquery<Subscription> subquery = query.subquery(Subscription.class);
            Root<Subscription> subqueryRoot = subquery.from(Subscription.class);
            subquery.select(subqueryRoot.get("target"))
                    .where(criteriaBuilder.equal(subqueryRoot.get("subscriber"), viewer.getId()));
            Predicate predicate = criteriaBuilder.and(root.get("owner").in(subquery));
            predicates.add(predicate);
        }

        Predicate visiblePredicate = criteriaBuilder.equal(root.get("state"), PublicationState.VISIBLE);
        predicates.add(visiblePredicate);
        query.orderBy(criteriaBuilder.desc(createdPath));
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
}
