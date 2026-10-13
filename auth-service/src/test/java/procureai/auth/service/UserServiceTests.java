package procureai.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import procureai.auth.dto.UserRequest;
import procureai.auth.dto.UserResponse;
import procureai.auth.entity.User;
import procureai.auth.entity.User.Role;
import procureai.auth.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTests {

    private UserRepository repository;
    private UserServiceImpl service;

    private final User admin = user(1L, "admin@company.com", Role.ADMIN);
    private final User manager = user(2L, "manager@company.com", Role.MANAGER);
    private final User employee = user(3L, "employee@company.com", Role.EMPLOYEE);

    @BeforeEach
    void setUp() {
        repository = mock(UserRepository.class);
        service = new UserServiceImpl(repository, new BCryptPasswordEncoder());
        for (User u : List.of(admin, manager, employee)) {
            when(repository.findByEmail(u.getEmail())).thenReturn(Optional.of(u));
            when(repository.findById(u.getId())).thenReturn(Optional.of(u));
        }
        when(repository.findAll(any(Sort.class))).thenReturn(List.of(admin, manager, employee));
        when(repository.save(any(User.class))).thenAnswer(call -> call.getArgument(0));
    }

    @Test
    void adminSeesManagersAndEmployees() {
        assertEquals(List.of(UserResponse.from(manager), UserResponse.from(employee)), service.getUsers(admin.getEmail()));
    }

    @Test
    void managerSeesOnlyEmployees() {
        assertEquals(List.of(UserResponse.from(employee)), service.getUsers(manager.getEmail()));
    }

    @Test
    void employeeAndMagasinierCannotListUsers() {
        User magasinier = user(4L, "magasinier@company.com", Role.MAGASINIER);
        when(repository.findByEmail(magasinier.getEmail())).thenReturn(Optional.of(magasinier));

        assertStatus(HttpStatus.FORBIDDEN, () -> service.getUsers(employee.getEmail()));
        assertStatus(HttpStatus.FORBIDDEN, () -> service.getUsers(magasinier.getEmail()));
    }

    @Test
    void adminCreatesManagerWithHashedPassword() {
        UserResponse created = service.createUser(admin.getEmail(), newUser(Role.MANAGER, "secret123"));
        assertEquals(Role.MANAGER, created.role());

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(repository).save(saved.capture());
        assertTrue(new BCryptPasswordEncoder().matches("secret123", saved.getValue().getPassword()));
    }

    @Test
    void adminCreatesMagasinier() {
        assertEquals(Role.MAGASINIER, service.createUser(admin.getEmail(), newUser(Role.MAGASINIER, "secret123")).role());
    }

    @Test
    void managerCannotCreateMagasinier() {
        assertStatus(HttpStatus.FORBIDDEN, () -> service.createUser(manager.getEmail(), newUser(Role.MAGASINIER, "secret123")));
    }

    @Test
    void onlyAdminCreatesComptable() {
        assertEquals(Role.COMPTABLE, service.createUser(admin.getEmail(), newUser(Role.COMPTABLE, "secret123")).role());
        assertStatus(HttpStatus.FORBIDDEN, () -> service.createUser(manager.getEmail(), newUser(Role.COMPTABLE, "secret123")));
    }

    @Test
    void managerCannotCreateManager() {
        assertStatus(HttpStatus.FORBIDDEN, () -> service.createUser(manager.getEmail(), newUser(Role.MANAGER, "secret123")));
        verify(repository, never()).save(any());
    }

    @Test
    void nobodyCreatesAdmin() {
        assertStatus(HttpStatus.FORBIDDEN, () -> service.createUser(admin.getEmail(), newUser(Role.ADMIN, "secret123")));
    }

    @Test
    void duplicateEmailIsRejected() {
        when(repository.existsByEmail("new@company.com")).thenReturn(true);
        assertStatus(HttpStatus.CONFLICT, () -> service.createUser(admin.getEmail(), newUser(Role.EMPLOYEE, "secret123")));
    }

    @Test
    void shortPasswordIsRejected() {
        assertStatus(HttpStatus.BAD_REQUEST, () -> service.createUser(manager.getEmail(), newUser(Role.EMPLOYEE, "123")));
    }

    @Test
    void managerCannotPromoteEmployee() {
        assertStatus(HttpStatus.FORBIDDEN,
                () -> service.updateUser(manager.getEmail(), employee.getId(), newUser(Role.MANAGER, "")));
    }

    @Test
    void updateKeepsPasswordWhenEmpty() {
        String oldHash = employee.getPassword();

        service.updateUser(manager.getEmail(), employee.getId(), newUser(Role.EMPLOYEE, ""));

        assertEquals(oldHash, employee.getPassword());
        assertEquals("new@company.com", employee.getEmail());
    }

    @Test
    void managerCannotDeleteManagerOrAdmin() {
        assertStatus(HttpStatus.FORBIDDEN, () -> service.deleteUser(manager.getEmail(), manager.getId()));
        assertStatus(HttpStatus.FORBIDDEN, () -> service.deleteUser(manager.getEmail(), admin.getId()));
        verify(repository, never()).delete(any());
    }

    @Test
    void adminDeletesEmployee() {
        service.deleteUser(admin.getEmail(), employee.getId());
        verify(repository).delete(employee);
    }

    private static User user(Long id, String email, Role role) {
        User user = new User();
        user.setId(id);
        user.setName(email);
        user.setEmail(email);
        user.setPassword("hash");
        user.setRole(role);
        return user;
    }

    private static UserRequest newUser(Role role, String password) {
        return new UserRequest("Nouveau", "new@company.com", password, null, null, role);
    }

    private static void assertStatus(HttpStatus status, Runnable action) {
        ResponseStatusException error = assertThrows(ResponseStatusException.class, action::run);
        assertEquals(status, error.getStatusCode());
    }
}
