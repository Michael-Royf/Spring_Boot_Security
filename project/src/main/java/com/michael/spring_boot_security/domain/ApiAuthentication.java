package com.michael.spring_boot_security.domain;

import com.michael.spring_boot_security.exception.payload.ApiException;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

import java.util.Collection;

public class ApiAuthentication extends AbstractAuthenticationToken {
    // Константы для защиты пароля и электронной почты от отображения
    private static final String PASSWORD_PROTECTED = "[PASSWORD PROTECTED]";
    private static final String EMAIL_PROTECTED = "[EMAIL PROTECTED]";
    private User user;
    private String email;
    private String password;
    private boolean authenticated;//флаг

    // Конструктор для создания аутентифицированного токена
    public ApiAuthentication(User user, Collection<? extends GrantedAuthority> authorities) {
        super(authorities); // Передача прав доступа
        this.user = user;
        this.password = PASSWORD_PROTECTED;
        this.email = EMAIL_PROTECTED;
        this.authenticated = true;
    }
    // Приватный конструктор для создания неаутентифицированного токена
    private ApiAuthentication(String email, String password) {
        super(AuthorityUtils.NO_AUTHORITIES);// Без прав доступа
        this.email = email;
        this.password = password;
        this.authenticated = false;
    }

    // Статический метод для создания неаутентифицированного экземпляра
    public static ApiAuthentication unauthenticated(String email, String password) {
        return new ApiAuthentication(email, password);
    }

    // Статический метод для создания аутентифицированного экземпляра
    public static ApiAuthentication authenticated(User user, Collection<? extends GrantedAuthority> authorities) {
        return new ApiAuthentication(user, authorities);
    }

    // Возвращает защищенные учетные данные (пароль)
    @Override
    public Object getCredentials() {
        return PASSWORD_PROTECTED;
    }

    // Возвращает главного пользователя (принципал)
    @Override
    public Object getPrincipal() {
        return this.user;
    }

    // Запрет на установку статуса аутентификации
    @Override
    public void setAuthenticated(boolean authenticated) {
        throw new ApiException("You cannot set authentication");
    }

    // Проверяет, аутентифицирован ли токен
    @Override
    public boolean isAuthenticated() {
        return this.authenticated;
    }

    // Метод для получения пароля (для внутреннего использования)
    public String getPassword() {
        return this.password;
    }

    // Метод для получения электронной почты (для внутреннего использования)
    public String getEmail() {
        return this.email;
    }
}
