package ru.vsu.cs.picstorm.entity;

import com.google.common.collect.ImmutableSet;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Set;
import java.util.stream.Collectors;

import static ru.vsu.cs.picstorm.entity.RoleAuthority.*;

@RequiredArgsConstructor
@Getter
public enum UserRole {
    BANNED(ImmutableSet.of()),
    ORDINARY(ImmutableSet.of(UPLOAD_AUTHORITY, SUBSCRIBE_AUTHORITY, REACT_AUTHORITY)),
    ADMIN(new ImmutableSet.Builder<RoleAuthority>()
            .addAll(ORDINARY.authorities)
            .add(BAN_PUBLICATION_AUTHORITY)
            .add(BAN_USER_AUTHORITY)
            .build()),

    SUPER_ADMIN(new ImmutableSet.Builder<RoleAuthority>()
            .addAll(ADMIN.authorities)
            .add(MANAGE_ADMINS_AUTHORITY)
            .build());

    private final Set<RoleAuthority> authorities;

    public Set<SimpleGrantedAuthority> getSimpleGrantedAuthorities() {
        return authorities.stream()
                .map(authority -> new SimpleGrantedAuthority(authority.name()))
                .collect(Collectors.toSet());
    }
}
