package com.study_companion.backend.controller.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.study_companion.backend.dto.AuthDto;
import com.study_companion.backend.dto.UserDto;
import com.study_companion.backend.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
@Tag(name = "Authentication", description = "User authentication and session management")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;

    public AuthController(AuthenticationConfiguration configuration, UserService userService) throws Exception {
        this.authenticationManager = configuration.getAuthenticationManager();
        this.userService = userService;
    }

    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Login user with username and password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User logged in"),
            @ApiResponse(responseCode = "400", description = "Invalid input or user not found", content = @Content(schema = @Schema(implementation = AuthDto.RegisterResponse.class), examples = {
                    @ExampleObject(name = "Invalid input", value = "{ \"message\": \"Invalid input\", \"error\": true }"),
                    @ExampleObject(name = "User not found", value = "{ \"message\": \"User not found\", \"error\": true }")
            }))
    })
    public ResponseEntity<AuthDto.LoginResponse> login(
            @Parameter(description = "Login request containing user username and password") @Valid @RequestBody AuthDto.LoginRequest request,
            @Parameter(hidden = true) HttpServletRequest httpRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.username(),
                            request.password()));

            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(authentication);
            SecurityContextHolder.setContext(securityContext);

            HttpSession session = httpRequest.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);

            return ResponseEntity.ok(new AuthDto.LoginResponse("Login successful", false));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new AuthDto.LoginResponse("Invalid credentials: " + e.getMessage(), true));
        }
    }

    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Create a new user account with name, email, username, and password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content(schema = @Schema(implementation = AuthDto.RegisterResponse.class), examples = @ExampleObject(value = "{ \"message\": \"Invalid input\", \"error\": true }"))),
            @ApiResponse(responseCode = "409", description = "User already exists", content = @Content(schema = @Schema(implementation = AuthDto.RegisterResponse.class), examples = @ExampleObject(value = "{ \"message\": \"User already exists\", \"error\": true }")))
    })
    public ResponseEntity<AuthDto.RegisterResponse> register(
            @Parameter(description = "Register request containing user name, email, username, and password") @Valid @RequestBody UserDto.CreateRequest request) {
        try {
            userService.createUser(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new AuthDto.RegisterResponse("User registered successfully", false));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new AuthDto.RegisterResponse("Registration failed: " +
                    e.getMessage(), true));
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user", description = "Logout user")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "User logged out") })
    public ResponseEntity<AuthDto.LogoutResponse> logout(@Parameter(hidden = true) HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(new AuthDto.LogoutResponse("Logged out successfully"));
    }
}