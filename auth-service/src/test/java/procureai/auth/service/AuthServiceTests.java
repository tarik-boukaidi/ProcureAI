package procureai.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import procureai.auth.dto.LoginRequest;
import procureai.auth.dto.RegisterRequest;
import procureai.auth.dto.UserResponse;
import procureai.auth.entity.User;
import procureai.auth.entity.User.Role;
import procureai.auth.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTests {

    private static final String SECRET = "test-secret-key-with-at-least-32-characters!";

    private UserRepository repository;
    private AuthServiceIml service;
    private final JwtService jwtService = new JwtService(SECRET);

    @BeforeEach
    void setUp() {
        repository = mock(UserRepository.class);
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        service = new AuthServiceIml(repository, encoder, jwtService);

        User amina = new User();
        amina.setId(11L);
        amina.setEmail("amina@company.com");
        amina.setPassword(encoder.encode("amina123"));
        amina.setRole(Role.COMPTABLE);
        when(repository.findByEmail("amina@company.com")).thenReturn(Optional.of(amina));
        when(repository.save(any(User.class))).thenAnswer(call -> call.getArgument(0));
    }

    @Test
    void loginReturnsTokenWithEmailAndRole() {
        String token = service.login(new LoginRequest("amina@company.com", "amina123")).token();

        assertEquals("amina@company.com", jwtService.extractEmail(token));
        assertEquals("COMPTABLE", jwtService.extractRole(token));
    }

    @Test
    void wrongPasswordAndUnknownEmailGiveTheSame401() {
        assertStatus(HttpStatus.UNAUTHORIZED, () -> service.login(new LoginRequest("amina@company.com", "wrong")));
        assertStatus(HttpStatus.UNAUTHORIZED, () -> service.login(new LoginRequest("nobody@company.com", "amina123")));
    }

    @Test
    void registerCreatesAnEmployee() {
        UserResponse user = service.register(new RegisterRequest("Yassine", "yassine@company.com", "secret123", null, null));

        assertEquals(Role.EMPLOYEE, user.role());
        assertEquals("yassine@company.com", user.email());
    }

    @Test
    void registerRejectsExistingEmail() {
        when(repository.existsByEmail("amina@company.com")).thenReturn(true);
        assertStatus(HttpStatus.CONFLICT,
                () -> service.register(new RegisterRequest("Amina", "amina@company.com", "secret123", null, null)));
    }

    private static void assertStatus(HttpStatus status, Runnable action) {
        ResponseStatusException error = assertThrows(ResponseStatusException.class, action::run);
        assertEquals(status, error.getStatusCode());
    }
}
