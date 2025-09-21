package ait.cohort51.g_51_security_jwt.security.service;

import ait.cohort51.g_51_security_jwt.domain.User;
import ait.cohort51.g_51_security_jwt.security.AuthUserDetails;
import ait.cohort51.g_51_security_jwt.security.dto.LoginRequestDto;
import ait.cohort51.g_51_security_jwt.security.dto.TokenResponseDto;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    private final UserService userService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final Map<String, String> refreshStorage;

    public AuthService(UserService userService, BCryptPasswordEncoder passwordEncoder, TokenService tokenService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        refreshStorage = new HashMap<>();
    }

    public TokenResponseDto login(LoginRequestDto loginRequest) {
        String username = loginRequest.getUsername();
        AuthUserDetails userDetails = (AuthUserDetails) userService.loadUserByUsername(username);
        User user = userDetails.getUser();

        if (passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            String accessToken = tokenService.generateAccessToken(username);
            String refreshToken = tokenService.generateRefreshToken(username);
            refreshStorage.put(username, refreshToken);
            return new TokenResponseDto(accessToken, refreshToken);
        } else {
            return null;
        }
    }

    public TokenResponseDto getAccessToken(HttpServletRequest request) {
        String refreshToken = tokenService.getTokenFromRequest(request, "Refresh-Token");

        if (tokenService.validateRefreshToken(refreshToken)) {
            Claims refreshClaims = tokenService.getRefreshClaims(refreshToken);
            String username = refreshClaims.getSubject();
            String savedRefreshToken = refreshStorage.get(username);

            if (savedRefreshToken != null && savedRefreshToken.equals(refreshToken)) {
                String accessToken = tokenService.generateAccessToken(username);
                return new TokenResponseDto(accessToken, refreshToken);
            }
        }
        return null;
    }
}
