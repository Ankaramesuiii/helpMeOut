package com.helpmeout.auth.security;

import com.helpmeout.common.exception.NotFoundException;
import com.helpmeout.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String phone) {
        return userRepository.findByPhone(phone)
                .map(UserPrincipal::new)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }
}

