package ait.cohort51.g_51_security_jwt.security.controller;

import ait.cohort51.g_51_security_jwt.security.dto.LoginRequestDto;
import ait.cohort51.g_51_security_jwt.security.dto.TokenResponseDto;
import ait.cohort51.g_51_security_jwt.security.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/login")
    public void login(@RequestBody LoginRequestDto loginDto, HttpServletResponse response) {
        TokenResponseDto tokens = service.login(loginDto);

        Cookie accessCookie = new Cookie("Access-Token", tokens.getAccessToken());
        accessCookie.setPath("/");
        accessCookie.setHttpOnly(true);
        response.addCookie(accessCookie);

        Cookie refreshCookie = new Cookie("Refresh-Token", tokens.getRefreshToken());
        refreshCookie.setPath("/");
        refreshCookie.setHttpOnly(true);
        response.addCookie(refreshCookie);
    }

    @GetMapping("/access")
    public void getNewAccessToken(HttpServletRequest request, HttpServletResponse response) {
        TokenResponseDto tokens = service.getAccessToken(request);

        Cookie accessCookie = new Cookie("Access-Token", tokens.getAccessToken());
        accessCookie.setPath("/");
        accessCookie.setHttpOnly(true);
        response.addCookie(accessCookie);
    }

    @GetMapping("/logout")
    public void logout(HttpServletResponse response) {
        Cookie accessCookie = new Cookie("Access-Token", null);
        accessCookie.setPath("/");
        accessCookie.setHttpOnly(true);
        accessCookie.setMaxAge(0);
        response.addCookie(accessCookie);

        Cookie refreshCookie = new Cookie("Refresh-Token", null);
        refreshCookie.setPath("/");
        refreshCookie.setHttpOnly(true);
        refreshCookie.setMaxAge(0);
        response.addCookie(refreshCookie);
    }
}
