package com.michael.spring_boot_security.service;

import com.michael.spring_boot_security.domain.User;
import com.michael.spring_boot_security.entity.CredentialEntity;
import com.michael.spring_boot_security.entity.RoleEntity;
import com.michael.spring_boot_security.enumerations.LoginType;
import com.michael.spring_boot_security.payload.request.RegistrationRequest;
import com.michael.spring_boot_security.payload.request.ResetPasswordRequest;
import com.michael.spring_boot_security.payload.request.RoleRequest;
import com.michael.spring_boot_security.payload.request.UpdatePasswordRequest;

import java.io.IOException;

public interface UserService {
    void createUser(RegistrationRequest request) throws IOException;

    RoleEntity getRoleName(String name);

    void verifyAccountKey(String key);

    void updateLoginAttempt(String email, LoginType loginType);

    User getUserByUserId(String userId);

    User getUserByEmail(String email);

    CredentialEntity getUserCredentialById(Long UserId);

    User setUpMfa(Long id);

    User cancelMfa(Long id);

    User verifyQrCode(String userId, String qrCode);

    void resetPassword(String email);

    User verifyPassword(String key);

    void updatePassword(ResetPasswordRequest resetPasswordRequest);

    void updatePassword(String userId, UpdatePasswordRequest updatePasswordRequest);

    User updateUser(String userId, RegistrationRequest registrationRequest);

    void updateRole(String userId, RoleRequest roleRequest);

    void toggleAccountExpired(String userId);

    void toggleAccountLocked(String userId);

    void toggleAccountEnabled(String userId);

    void toggleCredentialsExpired(String userId);
}
