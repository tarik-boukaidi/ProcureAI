package procureai.auth.service;

import procureai.auth.dto.LoginRequest;
import procureai.auth.dto.LoginResponse;
import procureai.auth.dto.RegisterRequest;
import procureai.auth.dto.UserResponse;

public interface AuthService {

    UserResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);
}
