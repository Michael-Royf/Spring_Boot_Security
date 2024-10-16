package com.michael.spring_boot_security.validations;

import com.michael.spring_boot_security.entity.UserEntity;
import com.michael.spring_boot_security.exception.payload.ApiException;

public class UserValidation {
    public static void verifyAccountStatus(UserEntity userEntity) {
        if (!userEntity.isEnabled()) {
            throw new ApiException("Account is disabled");
        }
        if (!userEntity.isAccountNonExpired()) {
            throw new ApiException("Account is expired");
        }
        if (!userEntity.isAccountNonLocked()) {
            throw new ApiException("Account is locked");
        }
    }
}
