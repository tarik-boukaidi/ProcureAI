package procureai.auth.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import procureai.auth.dto.LoginRequest;
import procureai.auth.dto.LoginResponse;
import procureai.auth.dto.RegisterRequest;
import procureai.auth.dto.UserResponse;
import procureai.auth.entity.User;
import procureai.auth.repository.UserRepository;

@Service
public class AuthServiceIml implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceIml(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public UserResponse register(RegisterRequest request) {
        String email = request.email().trim();
        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        User user = new User();
        user.setName(request.name());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setPhone(request.phone());
        user.setAddress(request.address());
        user.setRole(User.Role.EMPLOYEE);

        return UserResponse.from(userRepository.save(user));
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        // Meme reponse si l'email n'existe pas ou si le mot de passe est faux :
        // on ne revele pas quels emails ont un compte
        User user = userRepository.findByEmail(request.email().trim())
                .filter(u -> passwordEncoder.matches(request.password(), u.getPassword()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        return new LoginResponse(jwtService.generateToken(user.getEmail(), user.getRole().name(), user.getId()));
    }
}
