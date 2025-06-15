package com.chidituke.workout_tracker.security;

import com.chidituke.workout_tracker.model.User;
import com.chidituke.workout_tracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String emailOrUsername) throws UsernameNotFoundException {
        User user;

        if (emailOrUsername.contains("@")) {
            user = userRepository.findByEmail(emailOrUsername)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + emailOrUsername));
        } else {
            user = userRepository.findByUsername(emailOrUsername)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + emailOrUsername));
        }
        return UserPrincipal.create(user);
    }
}
