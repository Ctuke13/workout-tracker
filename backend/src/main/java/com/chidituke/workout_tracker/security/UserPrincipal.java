package com.chidituke.workout_tracker.security;

import com.chidituke.workout_tracker.model.user.User;
import com.chidituke.workout_tracker.model.user.enums.UserType;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Getter
public class UserPrincipal implements UserDetails {
    private Long id;
    private String username;
    private String email;
    private String password;
    private UserType userType;

    public UserPrincipal(Long id, String username, String email, String password, UserType userType) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.userType = userType;
    }

    public static UserPrincipal create(User user) {
        return new UserPrincipal(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                user.getUserType()
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (userType != null) {
            return List.of(new SimpleGrantedAuthority("ROLE_" + userType.name()));
        }
        return Collections.emptyList();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public boolean isProfessional() {
        return userType == UserType.PROFESSIONAL;
    }

    public boolean isAdmin() {
        return userType == UserType.ADMIN;
    }

    public boolean isRegular() {
        return userType == UserType.REGULAR;
    }
}