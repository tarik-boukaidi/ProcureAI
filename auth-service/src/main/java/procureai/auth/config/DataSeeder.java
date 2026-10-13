package procureai.auth.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import procureai.auth.entity.User;
import procureai.auth.repository.UserRepository;

// Au demarrage : cree le compte admin de test s'il n'existe pas encore
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        String email = "tarik@company.com";

        if (userRepository.existsByEmail(email)) {
            return;
        }

        User admin = new User();
        admin.setName("Tarik");
        admin.setEmail(email);
        admin.setPassword(passwordEncoder.encode("tarik123"));
        admin.setRole(User.Role.ADMIN);
        userRepository.save(admin);

        log.info("Compte de test cree : {}", email);
    }
}
