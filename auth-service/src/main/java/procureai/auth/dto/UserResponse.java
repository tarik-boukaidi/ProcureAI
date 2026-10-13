package procureai.auth.dto;

import procureai.auth.entity.User;
import procureai.auth.entity.User.Role;

// Ce qu'on renvoie d'un compte : jamais le mot de passe, meme hashe
public record UserResponse(
        Long id,
        String name,
        String email,
        String phone,
        String address,
        Role role) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(),
                user.getPhone(), user.getAddress(), user.getRole());
    }
}
