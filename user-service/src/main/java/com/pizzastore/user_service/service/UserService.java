package com.pizzastore.user_service.service;

import com.pizzastore.user_service.config.CustomUserDetailsService;
import com.pizzastore.user_service.dto.LoginDto;
import com.pizzastore.user_service.dto.LoginResponseDto;
import com.pizzastore.user_service.dto.UserDto;
import com.pizzastore.user_service.dto.UserRegistrationDto;
import com.pizzastore.user_service.entity.Role;
import com.pizzastore.user_service.entity.User;
import com.pizzastore.user_service.exception.InvalidCredentialsException;
import com.pizzastore.user_service.exception.UserAlreadyExistsException;
import com.pizzastore.user_service.exception.UserNotFoundException;
import com.pizzastore.user_service.repository.UserRepository;
import com.pizzastore.user_service.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    // User Registration
    public UserDto registerUser(UserRegistrationDto registrationDto) {
        logger.info("Registering new user: {}", registrationDto.getUsername());

        // Check if username already exists
        if (userRepository.existsByUsername(registrationDto.getUsername())) {
            throw new UserAlreadyExistsException("Username already exists: " + registrationDto.getUsername());
        }

        // Check if email already exists
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists: " + registrationDto.getEmail());
        }

        // Create new user
        User user = new User();
        user.setUsername(registrationDto.getUsername());
        user.setEmail(registrationDto.getEmail());
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        user.setFirstName(registrationDto.getFirstName());
        user.setLastName(registrationDto.getLastName());
        user.setPhone(registrationDto.getPhone());
        user.setAddress(registrationDto.getAddress());
        user.setRole(Role.CUSTOMER); // Default role is CUSTOMER

        User savedUser = userRepository.save(user);
        logger.info("User registered successfully: {}", savedUser.getUsername());

        return convertToUserDto(savedUser);
    }

    // Admin Registration
    public UserDto registerAdmin(UserRegistrationDto registrationDto) {
        logger.info("Registering new admin: {}", registrationDto.getUsername());

        UserDto userDto = registerUser(registrationDto);

        // Update role to ADMIN
        User user = userRepository.findByUsername(registrationDto.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        user.setRole(Role.ADMIN);
        User savedUser = userRepository.save(user);

        return convertToUserDto(savedUser);
    }

    // In UserService.java - Updated loginUser method with debugging
    public LoginResponseDto loginUser(LoginDto loginDto) {
        try {
            logger.info("User login attempt: {}", loginDto.getUsername());

            // Find user by username
            User user = userRepository.findByUsernameAndActiveTrue(loginDto.getUsername())
                    .orElseThrow(() -> {
                        logger.error("User not found: {}", loginDto.getUsername());
                        return new InvalidCredentialsException("Invalid username or password");
                    });

            logger.info("User found: {}", user.getUsername());

            // Verify password
            if (!passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
                logger.error("Password mismatch for user: {}", loginDto.getUsername());
                throw new InvalidCredentialsException("Invalid username or password");
            }

            logger.info("Password verified for user: {}", user.getUsername());

            // Generate JWT token
            UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
            logger.info("UserDetails loaded: {}", userDetails.getUsername());

            // Add custom claims
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", user.getUserId());
            claims.put("email", user.getEmail());
            claims.put("role", user.getRole().name());
            claims.put("firstName", user.getFirstName());
            claims.put("lastName", user.getLastName());

            logger.info("Claims prepared: {}", claims);

            String token = jwtUtil.generateTokenWithClaims(userDetails, claims);
            logger.info("JWT token generated successfully: {}", token != null ? "YES" : "NO");

            // Create response
            LoginResponseDto response = new LoginResponseDto(
                    token,
                    user.getUsername(),
                    user.getEmail(),
                    user.getRole().name(),
                    user.getUserId()
            );

            logger.info("Response created: {}", response.toString());
            logger.info("User logged in successfully: {}", user.getUsername());

            return response;

        } catch (InvalidCredentialsException e) {
            logger.error("Invalid credentials for user: {}", loginDto.getUsername());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during login for user: {}, Error: ", loginDto.getUsername(), e);
            throw new RuntimeException("Login failed due to internal error", e);
        }
    }


    // Get User Profile
    public UserDto getUserProfile(String username) {
        User user = userRepository.findByUsernameAndActiveTrue(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));

        return convertToUserDto(user);
    }

    // Update User Profile
    public UserDto updateUserProfile(String username, UserDto userDto) {
        logger.info("Updating user profile: {}", username);

        User user = userRepository.findByUsernameAndActiveTrue(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));

        // Update fields (excluding username, email, password, and role)
        if (userDto.getFirstName() != null) user.setFirstName(userDto.getFirstName());
        if (userDto.getLastName() != null) user.setLastName(userDto.getLastName());
        if (userDto.getPhone() != null) user.setPhone(userDto.getPhone());
        if (userDto.getAddress() != null) user.setAddress(userDto.getAddress());

        User savedUser = userRepository.save(user);
        logger.info("User profile updated successfully: {}", savedUser.getUsername());

        return convertToUserDto(savedUser);
    }

    // Change Password
    public void changePassword(String username, String oldPassword, String newPassword) {
        logger.info("Changing password for user: {}", username);

        User user = userRepository.findByUsernameAndActiveTrue(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));

        // Verify old password
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new InvalidCredentialsException("Invalid current password");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        logger.info("Password changed successfully for user: {}", username);
    }

    // Deactivate User Account
    public void deactivateUser(String username) {
        logger.info("Deactivating user account: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));

        user.setActive(false);
        userRepository.save(user);

        logger.info("User account deactivated: {}", username);
    }

    // Admin Functions

    // Get All Users (Admin only)
    public List<UserDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(this::convertToUserDto)
                .collect(Collectors.toList());
    }

    // Get Users by Role (Admin only)
    public List<UserDto> getUsersByRole(Role role) {
        List<User> users = userRepository.findByRole(role);
        return users.stream()
                .map(this::convertToUserDto)
                .collect(Collectors.toList());
    }

    // Get User Statistics (Admin only)
    public Map<String, Object> getUserStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalCustomers", userRepository.countCustomers());
        stats.put("totalAdmins", userRepository.countAdmins());
        stats.put("activeUsers", userRepository.findByActiveTrue().size());

        return stats;
    }

    // Helper method to convert User to UserDto
    private UserDto convertToUserDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setUserId(user.getUserId());
        userDto.setUsername(user.getUsername());
        userDto.setEmail(user.getEmail());
        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());
        userDto.setPhone(user.getPhone());
        userDto.setAddress(user.getAddress());
        userDto.setRole(user.getRole().name());
        userDto.setCreatedAt(user.getCreatedAt());
        userDto.setActive(user.getActive());

        return userDto;
    }

    // Validate JWT Token
    public boolean validateToken(String token) {
        return jwtUtil.validateJwtToken(token);
    }

    // Get Username from Token
    public String getUsernameFromToken(String token) {
        return jwtUtil.getUsernameFromJwtToken(token);
    }
}
