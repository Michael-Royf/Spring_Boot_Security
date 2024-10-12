package com.michael.spring_boot_security.service;

import com.michael.spring_boot_security.dto.User;
import com.michael.spring_boot_security.entity.RoleEntity;
import com.michael.spring_boot_security.enumerations.LoginType;
import com.michael.spring_boot_security.payload.request.RegistrationRequest;

import java.io.IOException;

public interface UserService {
    void createUser(RegistrationRequest request) throws IOException;

    RoleEntity getRoleName(String name);

    void verifyAccountKey(String key);

    void updateLoginAttempt(String email, LoginType loginType);

    User getUserByUserId(String userId);

}
