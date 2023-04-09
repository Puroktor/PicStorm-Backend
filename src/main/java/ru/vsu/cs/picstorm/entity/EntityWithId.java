package ru.vsu.cs.picstorm.entity;

import org.hibernate.Hibernate;

import java.util.Objects;

public abstract class EntityWithId {

    public abstract Long getId();
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        EntityWithId entity = (EntityWithId) o;
        return getId() != null && Objects.equals(getId(), entity.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
