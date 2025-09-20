package ait.cohort51.g_51_security_jwt.security.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class TokenService {

    private final SecretKey accessKey;
    private final SecretKey refreshKey;

    public TokenService(
            @Value("${key.access}") String accessPhrase,
            @Value("${key.refresh}") String refreshPhrase
    ) {

        // Генерация секретных ключей на основе секретных фраз
        accessKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessPhrase));
        refreshKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(refreshPhrase));
    }

    // Методы генерации токенов
    public String generateAccessToken(String username) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + 24 * 60 * 60 * 1000);

        return Jwts.builder()
                .subject(username)
                .expiration(expiration)
                .signWith(accessKey)
                .compact();
    }

    public String generateRefreshToken(String username) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + 7 * 24 * 60 * 60 * 1000);

        return Jwts.builder()
                .subject(username)
                .expiration(expiration)
                .signWith(refreshKey)
                .compact();
    }

    // Методы валидации токенов
    public boolean validateAccessToken(String accessToken) {
        return validateToken(accessToken, accessKey);
    }

    public boolean validateRefreshToken(String refreshToken) {
        return validateToken(refreshToken, refreshKey);
    }

    private boolean validateToken(String token, SecretKey key) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Методы, которые извлекают информацию о клиенте из токена
    public Claims getAccessClaims(String accessToken) {
        return getClaims(accessToken, accessKey);
    }

    public Claims getRefreshClaims(String refreshToken) {
        return getClaims(refreshToken, refreshKey);
    }

    private Claims getClaims(String token, SecretKey key) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String getTokenFromRequest(HttpServletRequest request, String tokenParamName) {
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(tokenParamName)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}