package procureai.auth.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import procureai.auth.dto.LoginRequest;
import procureai.auth.dto.LoginResponse;
import procureai.auth.dto.RegisterRequest;
import procureai.auth.dto.UserResponse;
import procureai.auth.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    // POST /api/auth/login  {"email": "...", "password": "..."}  ->  {"token": "eyJ..."}
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
