package ru.vsu.cs.picstorm.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.vsu.cs.picstorm.entity.User;
import ru.vsu.cs.picstorm.entity.UserRole;
import ru.vsu.cs.picstorm.repository.UserRepository;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String nickname) throws UsernameNotFoundException {
        User user = userRepository.findByNickname(nickname)
                .orElseThrow(() -> new UsernameNotFoundException("Имя пользователя не найдено"));
        if (user.getRole().equals(UserRole.BANNED)) {
            throw new UsernameNotFoundException("Пользователь заблокирован");
        }
        Collection<SimpleGrantedAuthority> authorities = user.getRole().getSimpleGrantedAuthorities();
        return new org.springframework.security.core.userdetails.User(user.getNickname(), user.getPasswordHash(), authorities);
    }
}
