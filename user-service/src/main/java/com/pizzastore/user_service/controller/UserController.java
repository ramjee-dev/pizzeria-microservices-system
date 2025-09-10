// UserController.java
package com.pizzastore.user_service.controller;

import com.pizzastore.user_service.dto.*;
import com.pizzastore.user_service.entity.Role;
import com.pizzastore.user_service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "Operations related to user management in Pizzeria application")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    @Operation(
            summary = "Register a new user",
            description = "Creates a new customer account in the Pizzeria system"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "User registered successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data or validation error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "User already exists with this username or email",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<UserDto> registerUser(
            @Valid @RequestBody
            @Parameter(description = "User registration details", required = true)
            UserRegistrationDto registrationDto) {

        logger.info("POST /api/users/register - Registering user: {}", registrationDto.getUsername());

        UserDto userDto = userService.registerUser(registrationDto);

        return new ResponseEntity<>(userDto, HttpStatus.CREATED);
    }

    @PostMapping("/admin/register")
    @Operation(
            summary = "Register a new admin user",
            description = "Creates a new admin account in the Pizzeria system (Admin only)",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Admin registered successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - Admin role required",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "User already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> registerAdmin(
            @Valid @RequestBody
            @Parameter(description = "Admin registration details", required = true)
            UserRegistrationDto registrationDto) {

        logger.info("POST /api/users/admin/register - Registering admin: {}", registrationDto.getUsername());

        UserDto userDto = userService.registerAdmin(registrationDto);

        return new ResponseEntity<>(userDto, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @Operation(
            summary = "User login",
            description = "Authenticates user and returns JWT token"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Login successful",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid credentials",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<LoginResponseDto> loginUser(
            @Valid @RequestBody
            @Parameter(description = "User login credentials", required = true)
            LoginDto loginDto) {

        logger.info("POST /api/users/login - Login attempt for user: {}", loginDto.getUsername());

        LoginResponseDto loginResponse = userService.loginUser(loginDto);

        return ResponseEntity.ok(loginResponse);
    }

    @GetMapping("/profile")
    @Operation(
            summary = "Get user profile",
            description = "Retrieves the profile information of the authenticated user",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Profile retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<UserDto> getUserProfile(
            @Parameter(hidden = true) Principal principal) {

        logger.info("GET /api/users/profile - Getting profile for user: {}", principal.getName());

        UserDto userDto = userService.getUserProfile(principal.getName());

        return ResponseEntity.ok(userDto);
    }

    @PutMapping("/profile")
    @Operation(
            summary = "Update user profile",
            description = "Updates the profile information of the authenticated user",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Profile updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<UserDto> updateUserProfile(
            @Parameter(hidden = true) Principal principal,
            @Valid @RequestBody
            @Parameter(description = "Updated user profile data", required = true)
            UserDto userDto) {

        logger.info("PUT /api/users/profile - Updating profile for user: {}", principal.getName());

        UserDto updatedUser = userService.updateUserProfile(principal.getName(), userDto);

        return ResponseEntity.ok(updatedUser);
    }

    @PostMapping("/change-password")
    @Operation(
            summary = "Change user password",
            description = "Changes the password for the authenticated user",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Password changed successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "string", example = "Password changed successfully")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid current password or unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<String> changePassword(
            @Parameter(hidden = true) Principal principal,
            @Valid @RequestBody
            @Parameter(description = "Password change request", required = true)
            ChangePasswordDto changePasswordDto) {

        logger.info("POST /api/users/change-password - Changing password for user: {}", principal.getName());

        userService.changePassword(
                principal.getName(),
                changePasswordDto.getOldPassword(),
                changePasswordDto.getNewPassword()
        );

        return ResponseEntity.ok("Password changed successfully");
    }

    @PostMapping("/deactivate")
    @Operation(
            summary = "Deactivate user account",
            description = "Deactivates the authenticated user's account",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Account deactivated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "string", example = "Account deactivated successfully")
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<String> deactivateUser(@Parameter(hidden = true) Principal principal) {
        logger.info("POST /api/users/deactivate - Deactivating account for user: {}", principal.getName());

        userService.deactivateUser(principal.getName());

        return ResponseEntity.ok("Account deactivated successfully");
    }

    @GetMapping("/validate-token")
    @Operation(
            summary = "Validate JWT token",
            description = "Validates if the provided JWT token is valid and not expired"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Token validation result",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "boolean", example = "true")
                    )
            )
    })
    public ResponseEntity<Boolean> validateToken(
            @RequestParam
            @Parameter(description = "JWT token to validate", required = true, example = "eyJhbGciOiJIUzUxMiJ9...")
            String token) {

        logger.info("GET /api/users/validate-token - Validating token");

        boolean isValid = userService.validateToken(token);

        return ResponseEntity.ok(isValid);
    }

    @GetMapping("/username-from-token")
    @Operation(
            summary = "Get username from JWT token",
            description = "Extracts and returns the username from a valid JWT token"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Username retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "string", example = "john_doe")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<String> getUsernameFromToken(
            @RequestParam
            @Parameter(description = "JWT token", required = true, example = "eyJhbGciOiJIUzUxMiJ9...")
            String token) {

        logger.info("GET /api/users/username-from-token - Getting username from token");

        String username = userService.getUsernameFromToken(token);

        return ResponseEntity.ok(username);
    }

    // Admin only endpoints

    @GetMapping("/all")
    @Operation(
            summary = "Get all users",
            description = "Retrieves a list of all users in the system (Admin only)",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Users retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "array", implementation = UserDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - Admin role required",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        logger.info("GET /api/users/all - Getting all users");

        List<UserDto> users = userService.getAllUsers();

        return ResponseEntity.ok(users);
    }

    @GetMapping("/customers")
    @Operation(
            summary = "Get all customers",
            description = "Retrieves a list of all customer users (Admin only)",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Customers retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "array", implementation = UserDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - Admin role required",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDto>> getAllCustomers() {
        logger.info("GET /api/users/customers - Getting all customers");

        List<UserDto> customers = userService.getUsersByRole(Role.CUSTOMER);

        return ResponseEntity.ok(customers);
    }

    @GetMapping("/admins")
    @Operation(
            summary = "Get all admins",
            description = "Retrieves a list of all admin users (Admin only)",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Admins retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "array", implementation = UserDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - Admin role required",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDto>> getAllAdmins() {
        logger.info("GET /api/users/admins - Getting all admins");

        List<UserDto> admins = userService.getUsersByRole(Role.ADMIN);

        return ResponseEntity.ok(admins);
    }

    @GetMapping("/statistics")
    @Operation(
            summary = "Get user statistics",
            description = "Retrieves statistical information about users (Admin only)",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Statistics retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    type = "object",
                                    example = "{\"totalUsers\": 150, \"totalCustomers\": 140, \"totalAdmins\": 10, \"activeUsers\": 145}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - Admin role required",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getUserStatistics() {
        logger.info("GET /api/users/statistics - Getting user statistics");

        Map<String, Object> statistics = userService.getUserStatistics();

        return ResponseEntity.ok(statistics);
    }

    // Error Response class for documentation
    public static class ErrorResponse {
        @Schema(description = "HTTP status code", example = "400")
        private int status;

        @Schema(description = "Error type", example = "Validation Error")
        private String error;

        @Schema(description = "Error message", example = "Username is required")
        private String message;

        @Schema(description = "Request path", example = "/api/users/register")
        private String path;

        @Schema(description = "Timestamp", example = "2025-09-10T11:53:00")
        private String timestamp;

        // Constructors, getters, and setters
    }
}
