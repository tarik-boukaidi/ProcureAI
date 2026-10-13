package procureai.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import procureai.auth.dto.UserRequest;
import procureai.auth.dto.UserResponse;
import procureai.auth.service.UserService;

import java.security.Principal;
import java.util.List;

// Gestion des comptes : ADMIN -> tous les comptes sauf les admins, MANAGER -> employes.
// L'utilisateur connecte vient du JWT verifie par JwtAuthenticationFilter (principal = son email).
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping
    public List<UserResponse> getUsers(Principal principal) {
        return service.getUsers(principal.getName());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(@RequestBody UserRequest request, Principal principal) {
        return service.createUser(principal.getName(), request);
    }

    @PutMapping("/{id}")
    public UserResponse updateUser(@PathVariable Long id, @RequestBody UserRequest request, Principal principal) {
        return service.updateUser(principal.getName(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id, Principal principal) {
        service.deleteUser(principal.getName(), id);
    }
}
