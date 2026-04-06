package com.helpmeout.auth.service;

import com.helpmeout.auth.dto.AuthResponse;
import com.helpmeout.auth.dto.LoginRequest;
import com.helpmeout.auth.dto.RegisterRequest;
import com.helpmeout.auth.dto.VerifyPhoneRequest;
import com.helpmeout.auth.security.UserPrincipal;
import com.helpmeout.common.exception.BadRequestException;
import com.helpmeout.common.exception.NotFoundException;
import com.helpmeout.common.exception.UnauthorizedException;
import com.helpmeout.user.entity.PhoneVerificationStatus;
import com.helpmeout.user.entity.User;
import com.helpmeout.user.entity.UserProfile;
import com.helpmeout.user.entity.UserRole;
import com.helpmeout.user.repository.UserProfileRepository;
import com.helpmeout.user.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository, UserProfileRepository userProfileRepository, PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new BadRequestException("Phone number already registered");
        }

        User user = new User();
        user.setPhone(request.getPhone());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.USER);
        user.setPhoneVerificationStatus(PhoneVerificationStatus.PENDING);
        user.setIsActive(true);

        User savedUser = userRepository.save(user);

        UserProfile profile = new UserProfile();
        profile.setUser(savedUser);
        profile.setFullName(request.getFullName());
        profile.setCity(request.getCity());
        profile.setArea(request.getArea());
        userProfileRepository.save(profile);

        String token = jwtService.generateToken(new UserPrincipal(savedUser));

        return AuthResponse.builder()
                .userId(savedUser.getId())
                .phone(savedUser.getPhone())
                .fullName(profile.getFullName())
                .token(token)
                .role(savedUser.getRole().name())
                .phoneVerificationStatus(savedUser.getPhoneVerificationStatus().name())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getPhone(), request.getPassword())
        );

        User user = userRepository.findByPhone(request.getPhone())
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new UnauthorizedException("User account is inactive");
        }

        UserProfile profile = userProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new NotFoundException("User profile not found"));

        String token = jwtService.generateToken(new UserPrincipal(user));

        return AuthResponse.builder()
                .userId(user.getId())
                .phone(user.getPhone())
                .fullName(profile.getFullName())
                .token(token)
                .role(user.getRole().name())
                .phoneVerificationStatus(user.getPhoneVerificationStatus().name())
                .build();
    }

    @Transactional
    public void verifyPhone(VerifyPhoneRequest request) {
        User user = userRepository.findByPhone(request.getPhone())
                .orElseThrow(() -> new NotFoundException("User not found"));

        // MVP mock verification
        if (!"1234".equals(request.getCode())) {
            throw new BadRequestException("Invalid verification code");
        }

        user.setPhoneVerificationStatus(PhoneVerificationStatus.VERIFIED);
        userRepository.save(user);
    }
}
