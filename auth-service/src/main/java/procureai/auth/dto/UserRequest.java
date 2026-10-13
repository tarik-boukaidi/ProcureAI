package procureai.auth.dto;

import procureai.auth.entity.User.Role;

// Creation / modification d'un compte par un admin ou un manager.
// password : obligatoire a la creation, vide a la modification = on garde l'ancien.
public record UserRequest(
        String name,
        String email,
        String password,
        String phone,
        String address,
        Role role) {
}
