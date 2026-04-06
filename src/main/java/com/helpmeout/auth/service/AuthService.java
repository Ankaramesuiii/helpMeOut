package com.helpmeout.auth.service;

import com.helpmeout.auth.dto.AuthResponse;
import com.helpmeout.auth.dto.LoginRequest;
import com.helpmeout.auth.dto.RegisterRequest;
import com.helpmeout.auth.dto.VerifyPhoneRequest;
import com.helpmeout.auth.security.UserPrincipal;
import com.helpmeout.common.exception.AuthenticationFailedException;
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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
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
        // Check if phone number is already registered
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new BadRequestException("Phone number already registered. Please use a different phone number or proceed to login.");
        }

        try {
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
        } catch (Exception e) {
            throw new BadRequestException("Registration failed: " + e.getMessage());
        }
    }

    public AuthResponse login(LoginRequest request) {
        try {
            // Attempt to authenticate with the provided credentials
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getPhone(), request.getPassword())
            );
        } catch (InternalAuthenticationServiceException e) {
            // This exception occurs when the user doesn't exist (thrown by CustomUserDetailsService)
            throw new AuthenticationFailedException(
                    "Invalid phone number or password",
                    "INVALID_CREDENTIALS",
                    "User with phone number not found"
            );
        } catch (BadCredentialsException e) {
            // This exception occurs when the password is incorrect
            throw new AuthenticationFailedException(
                    "Invalid phone number or password",
                    "INVALID_CREDENTIALS",
                    "Password mismatch"
            );
        } catch (org.springframework.security.core.AuthenticationException e) {
            // Handle any other Spring Security authentication exceptions
            throw new AuthenticationFailedException(
                    "Authentication failed: " + e.getMessage(),
                    "AUTH_ERROR",
                    e.getMessage()
            );
        }

        // Fetch the user from database (should exist at this point)
        User user = userRepository.findByPhone(request.getPhone())
                .orElseThrow(() -> new AuthenticationFailedException(
                        "User not found",
                        "USER_NOT_FOUND"
                ));

        // Check if account is active
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new AuthenticationFailedException(
                    "User account is inactive",
                    "ACCOUNT_INACTIVE",
                    "Please contact support to reactivate your account"
            );
        }

        // Fetch user profile
        UserProfile profile = userProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new AuthenticationFailedException(
                        "User profile not found",
                        "PROFILE_NOT_FOUND"
                ));

        // Generate JWT token
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
