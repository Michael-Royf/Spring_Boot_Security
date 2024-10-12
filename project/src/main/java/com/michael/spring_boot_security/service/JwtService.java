package com.michael.spring_boot_security.service;

import com.michael.spring_boot_security.domain.Token;
import com.michael.spring_boot_security.domain.TokenData;
import com.michael.spring_boot_security.domain.User;
import com.michael.spring_boot_security.enumerations.TokenType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Optional;
import java.util.function.Function;

public interface JwtService {
    String createToken(User user, Function<Token, String> tokenFunction);

    Optional<String> extractToken(HttpServletRequest request, String cookieName);

    void addCookie(HttpServletResponse response, User user, TokenType tokenType);

    <T> T getTokenData(String token, Function<TokenData, T> tokenFunction);

    void removeCookie(HttpServletRequest request, HttpServletResponse response, String cookieName);
}
