package procureai.auth.service;

import procureai.auth.dto.UserRequest;
import procureai.auth.dto.UserResponse;

import java.util.List;

// Gestion des comptes : "actorEmail" est l'utilisateur connecte qui fait l'action
public interface UserService {

    List<UserResponse> getUsers(String actorEmail);

    UserResponse createUser(String actorEmail, UserRequest request);

    UserResponse updateUser(String actorEmail, Long id, UserRequest request);

    void deleteUser(String actorEmail, Long id);
}
