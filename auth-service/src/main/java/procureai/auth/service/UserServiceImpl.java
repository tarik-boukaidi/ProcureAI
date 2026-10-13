package procureai.auth.service;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import procureai.auth.dto.UserRequest;
import procureai.auth.dto.UserResponse;
import procureai.auth.entity.User;
import procureai.auth.entity.User.Role;
import procureai.auth.repository.UserRepository;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private static final int MIN_PASSWORD_LENGTH = 6;

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    // ADMIN gere tous les comptes sauf les admins, MANAGER gere seulement les employes
    static boolean canManage(Role actorRole, Role targetRole) {
        if (actorRole == Role.ADMIN) return targetRole != null && targetRole != Role.ADMIN;
        if (actorRole == Role.MANAGER) return targetRole == Role.EMPLOYEE;
        return false;
    }

    @Override
    public List<UserResponse> getUsers(String actorEmail) {
        User actor = getActor(actorEmail);
        if (actor.getRole() != Role.ADMIN && actor.getRole() != Role.MANAGER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        return repository.findAll(Sort.by("id")).stream()
                .filter(user -> canManage(actor.getRole(), user.getRole()))
                .map(UserResponse::from)
                .toList();
    }

    @Override
    public UserResponse createUser(String actorEmail, UserRequest request) {
        checkCanManage(getActor(actorEmail), request.role());
        checkFields(request);
        if (!isValidPassword(request.password())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must have at least 6 characters");
        }
        String email = request.email().trim();
        if (repository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        User user = new User();
        user.setName(request.name());
        user.setEmail(email);
        user.setPhone(request.phone());
        user.setAddress(request.address());
        user.setRole(request.role());
        user.setPassword(passwordEncoder.encode(request.password()));
        return UserResponse.from(repository.save(user));
    }

    @Override
    public UserResponse updateUser(String actorEmail, Long id, UserRequest request) {
        User actor = getActor(actorEmail);
        User existing = getManagedUser(actor, id);
        checkCanManage(actor, request.role());
        checkFields(request);

        String email = request.email().trim();
        if (!email.equals(existing.getEmail()) && repository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        existing.setName(request.name());
        existing.setEmail(email);
        existing.setPhone(request.phone());
        existing.setAddress(request.address());
        existing.setRole(request.role());

        // Mot de passe vide = on garde l'ancien
        if (request.password() != null && !request.password().isBlank()) {
            if (!isValidPassword(request.password())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must have at least 6 characters");
            }
            existing.setPassword(passwordEncoder.encode(request.password()));
        }
        return UserResponse.from(repository.save(existing));
    }

    @Override
    public void deleteUser(String actorEmail, Long id) {
        repository.delete(getManagedUser(getActor(actorEmail), id));
    }

    private User getActor(String email) {
        return repository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unknown user"));
    }

    // 404 si le compte n'existe pas, 403 si l'utilisateur connecte n'a pas le droit de le gerer
    private User getManagedUser(User actor, Long id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        checkCanManage(actor, user.getRole());
        return user;
    }

    private void checkCanManage(User actor, Role targetRole) {
        if (!canManage(actor.getRole(), targetRole)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    actor.getRole() + " cannot manage role: " + targetRole);
        }
    }

    private void checkFields(UserRequest request) {
        if (isBlank(request.name()) || isBlank(request.email()) || !request.email().contains("@")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name and a valid email are required");
        }
    }

    private boolean isValidPassword(String password) {
        return password != null && password.length() >= MIN_PASSWORD_LENGTH;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
