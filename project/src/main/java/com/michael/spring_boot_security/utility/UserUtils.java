package com.michael.spring_boot_security.utility;

import com.michael.spring_boot_security.constans.AppConstants;
import com.michael.spring_boot_security.domain.User;
import com.michael.spring_boot_security.entity.CredentialEntity;
import com.michael.spring_boot_security.entity.RoleEntity;
import com.michael.spring_boot_security.entity.UserEntity;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;

import static com.michael.spring_boot_security.constans.AppConstants.NINETY_DAYS;

public class UserUtils {

    public static User fromUserEntity(UserEntity userEntity,
                                      RoleEntity role,
                                      CredentialEntity credentialEntity) {
        User user = new User();
        BeanUtils.copyProperties(userEntity, user);
        user.setLastLogin(userEntity.getLastLogin().toString());
        user.setCredentialsNonExpired(isCredentialNonExpired(credentialEntity));
        user.setCreatedAt(userEntity.getCreatedAt().toString());
        user.setUpdatedAt(userEntity.getUpdatedAt().toString());
        user.setRole(role.getName());
        user.setAuthorities(role.getAuthorities().getValue());
        return user;
    }


    private static boolean isCredentialNonExpired(CredentialEntity credentialEntity) {
        return credentialEntity.getUpdatedAt().plusDays(NINETY_DAYS).isAfter(LocalDateTime.now());
    }
}
